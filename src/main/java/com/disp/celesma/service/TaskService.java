package com.disp.celesma.service;

import com.disp.celesma.dto.task.TaskRequest;
import com.disp.celesma.dto.task.TaskResponse;
import com.disp.celesma.event.TaskCreatedEvent;
import com.disp.celesma.event.TaskStatusChangedEvent;
import com.disp.celesma.model.Task;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.TaskStatus;
import com.disp.celesma.repository.ProjectRepository;
import com.disp.celesma.repository.TaskRepository;
import com.disp.celesma.service.interfaces.IProjectMemberService;
import com.disp.celesma.service.interfaces.ITaskService;
import com.disp.celesma.service.interfaces.IUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService implements ITaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final IProjectMemberService projectMemberService;
    private final ApplicationEventPublisher eventPublisher;
    private final IUserService userService;

    @Override
    @Transactional
    public TaskResponse createTask(Long projectId, User creator, TaskRequest request) {
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project %d not found".formatted(projectId)));
        var assignee = userService.getUserById(request.assigneeId());

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
        return TaskResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        return TaskResponse.from(findById(taskId));
    }

    @Override
    @Transactional(readOnly = true)
    public Task getTaskEntityById(Long taskId) {
        return findById(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, TaskRequest request, User caller, Long projectId) {
        var task = findById(taskId);
        if (!canModify(task, caller, projectId)) {
            throw new AccessDeniedException("Нет прав для редактирования задачи");
        }

        var assignee = userService.getUserById(request.assigneeId());

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setAssignee(assignee);
        task.setPriority(request.priority());
        task.setEndDate(request.endDate());

        return TaskResponse.from(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse changeStatus(Long taskId, TaskStatus newStatus, User caller, Long projectId) {
        var task = findById(taskId);
        var oldStatus = task.getStatus();

        if (oldStatus == newStatus) return TaskResponse.from(task);

        if (!canModify(task, caller, projectId)) {
            throw new AccessDeniedException("Нет прав для изменения статуса задачи");
        }

        if (newStatus == TaskStatus.COMPLETED) {
            task.setReviewedBy(caller);
        } else if (isTerminalStatus(oldStatus) && !isTerminalStatus(newStatus)) {
            task.setReviewedBy(null);
        }

        task.setStatus(newStatus);
        var saved = taskRepository.save(task);

        eventPublisher.publishEvent(new TaskStatusChangedEvent(this, saved, oldStatus, newStatus));
        return TaskResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    @Override
    @Transactional
    public void reassignAndHoldTasks(Long projectId, Long fromUserId, User toUser) {
        var tasks = taskRepository.findByProjectIdAndAssigneeId(projectId, fromUserId);
        tasks.forEach(t -> {
            t.setAssignee(toUser);
            t.setStatus(TaskStatus.ON_HOLD);
        });
        taskRepository.saveAll(tasks);
    }

    private Task findById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task %d not found".formatted(taskId)));
    }

    private boolean canModify(Task task, User caller, Long projectId) {
        return projectMemberService.isPrivileged(projectId, caller.getId())
                || task.getCreator().getId().equals(caller.getId())
                || (task.getAssignee() != null && task.getAssignee().getId().equals(caller.getId()));
    }

    private boolean isTerminalStatus(TaskStatus status) {
        return status == TaskStatus.COMPLETED
                || status == TaskStatus.CANCELED
                || status == TaskStatus.ON_HOLD;
    }

}
