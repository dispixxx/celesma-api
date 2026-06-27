package com.disp.celesma.service;

import com.disp.celesma.dto.task.TaskCreateRequest;
import com.disp.celesma.dto.task.TaskResponse;
import com.disp.celesma.dto.task.TaskUpdateRequest;
import com.disp.celesma.dto.task.attachment.TaskAttachmentResponse;
import com.disp.celesma.event.member.MemberExitedProjectEvent;
import com.disp.celesma.event.task.TaskCreatedEvent;
import com.disp.celesma.event.task.TaskStatusChangedEvent;
import com.disp.celesma.mapper.TaskAttachmentMapper;
import com.disp.celesma.mapper.TaskMapper;
import com.disp.celesma.model.Project;
import com.disp.celesma.model.Task;
import com.disp.celesma.model.TaskAttachment;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.TaskStatus;
import com.disp.celesma.repository.ProjectRepository;
import com.disp.celesma.repository.TaskAttachmentRepository;
import com.disp.celesma.repository.TaskRepository;
import com.disp.celesma.s3.service.interfaces.IStorageService;
import com.disp.celesma.service.interfaces.IProjectMemberService;
import com.disp.celesma.service.interfaces.ITaskHistoryService;
import com.disp.celesma.service.interfaces.ITaskService;
import com.disp.celesma.service.interfaces.IUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService implements ITaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    private final ITaskHistoryService taskHistoryService;
    private final IProjectMemberService projectMemberService;
    private final IUserService userService;

    private final ApplicationEventPublisher eventPublisher;
    private final TaskMapper taskMapper;
    private final IStorageService storageService;
    private final TaskAttachmentRepository taskAttachmentRepository;

    private final TaskAttachmentMapper taskAttachmentMapper;

    private Project getProjectEntityOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project %d not found".formatted(projectId)));
    }

    @Override
    public Task getTaskEntityById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task %d not found".formatted(taskId)));
    }

    @Override
    @Transactional
    public TaskResponse createTaskAndSave(Long projectId, User creator, TaskCreateRequest request) {
        var project = getProjectEntityOrThrow(projectId);
        var assignee = userService.getUserEntityById(request.assigneeId());

        projectMemberService.validateIsMember(projectId, assignee.getId());

        var task = Task.builder()
                .title(request.title())
                .description(request.description())
                .createdAt(LocalDateTime.now())
                .assignee(assignee)
                .endDate(request.endDate())
                .project(project)
                .creator(creator)
                .priority(request.priority())
                .build();

        var saved = taskRepository.save(task);


        eventPublisher.publishEvent(new TaskCreatedEvent(this, saved));
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        return taskMapper.toResponse(getTaskEntityById(taskId));
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request, User caller) {
        var task = getTaskEntityById(taskId);
        checkModifyAccess(task, caller);

        var assignee = userService.getUserEntityById(request.assigneeId());

        var changes = taskHistoryService.diffTask(task, request, assignee);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setAssignee(assignee);
        task.setPriority(request.priority());
        task.setEndDate(request.endDate());

        var saved = taskRepository.save(task);

        if (!changes.isEmpty()) {
            taskHistoryService.record(saved, caller, changes);
        }

        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TaskResponse changeStatus(Long taskId, TaskStatus newStatus, User caller) {
        return changeStatusInner(taskId, newStatus, caller);
    }

    @Override
    @Transactional
    public TaskResponse changeStatus(Long taskId, TaskStatus newStatus, String callerUsername) {
        var caller = userService.getUserEntityByUsername(callerUsername);
        return changeStatusInner(taskId, newStatus, caller);
    }

    private TaskResponse changeStatusInner(Long taskId, TaskStatus newStatus, User caller) {
        var task = getTaskEntityById(taskId);

        projectMemberService.validateIsMember(task.getProject().getId(), caller.getId());

        var oldStatus = task.getStatus();

        if (oldStatus == newStatus) return taskMapper.toResponse(task);

        checkModifyAccess(task, caller);

        if (newStatus == TaskStatus.COMPLETED) {
            task.setReviewedBy(caller);
        } else if (isTerminalStatus(oldStatus) && !isTerminalStatus(newStatus)) {
            task.setReviewedBy(null);
        }

        task.setStatus(newStatus);
        var saved = taskRepository.save(task);

        eventPublisher.publishEvent(new TaskStatusChangedEvent(this, saved, oldStatus, newStatus));
        return taskMapper.toResponse(saved);
    }


    //TODO: VALIDATE AND PROTECT FROM DELETING TASKS BY NON PRIVILEGED USERS
    @Override
    @Transactional
    public void deleteTask(Long taskId, User caller) {
        checkDeleteAccess(getTaskEntityById(taskId), caller);
        taskHistoryService.deleteAllByTaskId(taskId);
        taskRepository.deleteById(taskId);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberExit(MemberExitedProjectEvent event) {
        reassignAndHoldTasks(event.getProjectId(), event.getExitedUserId(), event.getCaller());
    }

    @Override
    public void reassignAndHoldTasks(Long projectId, Long fromUserId, User toUser) {
        var tasks = taskRepository.findByProjectIdAndAssigneeId(projectId, fromUserId);
        tasks.forEach(t -> {
            t.setAssignee(toUser);
            t.setStatus(TaskStatus.ON_HOLD);
        });
        taskRepository.saveAll(tasks);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TaskAttachmentResponse> getAttachments(Long taskId) {
        return taskAttachmentRepository.findByTaskIdWithDetails(taskId)
                .stream()
                .map(taskAttachmentMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public TaskAttachmentResponse uploadAttachment(Long taskId, MultipartFile file, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        String url = storageService.uploadTaskAttachment(file, taskId);

        TaskAttachment attachment = TaskAttachment.builder()
                .task(task)
                .uploadedBy(user)
                .fileUrl(url)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .build();

        var saved = taskAttachmentRepository.save(attachment);
        return taskAttachmentMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public void deleteAttachment(Long taskId, Long attachmentId, User user) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));

        boolean isUploader = attachment.getUploadedBy().getId().equals(user.getId());
        boolean isOwnerOrAdmin = projectMemberService.isPrivileged(
                attachment.getTask().getProject().getId(), user.getId()
        );

        if (!isUploader && !isOwnerOrAdmin) {
            throw new AccessDeniedException("Not allowed to delete this attachment");
        }

        storageService.deleteFile(attachment.getFileUrl());
        taskAttachmentRepository.delete(attachment);
    }

    // ─────────────────────────────────────────────
    // Вспомогательные методы
    // ─────────────────────────────────────────────

    private void checkModifyAccess(Task task, User caller) {
        var projectId = task.getProject().getId();
        boolean allowed = projectMemberService.isPrivileged(projectId, caller.getId())
                || task.getCreator().getId().equals(caller.getId())
                || (task.getAssignee() != null && task.getAssignee().getId().equals(caller.getId()));
        if (!allowed) {
            throw new AccessDeniedException("Нет прав для изменения задачи");
        }
    }

    private void checkDeleteAccess(Task task, User caller) {
        var projectId = task.getProject().getId();
        boolean allowed = projectMemberService.isPrivileged(projectId, caller.getId());
        if (!allowed) {
            throw new AccessDeniedException("Нет прав для удаления задачи");
        }
    }

    private boolean isTerminalStatus(TaskStatus status) {
        return status == TaskStatus.COMPLETED
                || status == TaskStatus.CANCELED
                || status == TaskStatus.ON_HOLD;
    }
}