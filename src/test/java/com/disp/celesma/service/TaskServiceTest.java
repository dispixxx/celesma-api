package com.disp.celesma.service;

import com.disp.celesma.dto.task.TaskCreateRequest;
import com.disp.celesma.dto.task.TaskResponse;
import com.disp.celesma.dto.task.TaskUpdateRequest;
import com.disp.celesma.mapper.TaskAttachmentMapper;
import com.disp.celesma.mapper.TaskMapper;
import com.disp.celesma.model.Project;
import com.disp.celesma.model.Task;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.TaskPriority;
import com.disp.celesma.model.enums.TaskStatus;
import com.disp.celesma.repository.ProjectRepository;
import com.disp.celesma.repository.TaskAttachmentRepository;
import com.disp.celesma.repository.TaskRepository;
import com.disp.celesma.s3.service.interfaces.IStorageService;
import com.disp.celesma.service.interfaces.IProjectMemberService;
import com.disp.celesma.service.interfaces.ITaskHistoryService;
import com.disp.celesma.service.interfaces.IUserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock ProjectRepository projectRepository;
    @Mock ITaskHistoryService taskHistoryService;
    @Mock IProjectMemberService projectMemberService;
    @Mock IUserService userService;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock TaskMapper taskMapper;
    @Mock IStorageService storageService;
    @Mock TaskAttachmentRepository taskAttachmentRepository;
    @Mock TaskAttachmentMapper taskAttachmentMapper;
    @Mock SimpMessagingTemplate messagingTemplate;

    @InjectMocks TaskService taskService;

    private User creator;
    private User assignee;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        creator = User.builder().id(1L).username("creator").build();
        assignee = User.builder().id(2L).username("assignee").build();
        project = Project.builder().id(10L).name("Test Project").build();
        task = Task.builder()
                .id(100L)
                .title("Test Task")
                .project(project)
                .creator(creator)
                .assignee(assignee)
                .status(TaskStatus.NEW)
                .build();
    }

    @Test
    void createTask_success() {
        var request = new TaskCreateRequest("Title", "Desc", assignee.getId(), TaskPriority.HIGH, LocalDate.now());
        var response = mock(TaskResponse.class);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.getUserEntityById(assignee.getId())).thenReturn(assignee);
        when(projectMemberService.validateIsMember(project.getId(), assignee.getId())).thenReturn(true);
        when(taskRepository.save(any())).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(response);

        var result = taskService.createTaskAndSave(project.getId(), creator, request);

        assertThat(result).isEqualTo(response);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void createTask_projectNotFound_throws() {
        var request = new TaskCreateRequest("Title", "Desc", assignee.getId(), TaskPriority.HIGH, LocalDate.now());
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTaskAndSave(99L, creator, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTaskById_notFound_throws() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTasksByProject_returnsMappedList() {
        var response = mock(TaskResponse.class);
        when(taskRepository.findByProjectId(project.getId())).thenReturn(List.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        var result = taskService.getTasksByProject(project.getId());

        assertThat(result).containsExactly(response);
    }

    @Test
    void changeStatus_sameStatus_returnsWithoutSaving() {
        task.setStatus(TaskStatus.NEW);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(projectMemberService.validateIsMember(project.getId(), creator.getId())).thenReturn(true);
        when(taskMapper.toResponse(task)).thenReturn(mock(TaskResponse.class));

        taskService.changeStatus(task.getId(), TaskStatus.NEW, creator);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void changeStatus_toCompleted_setsReviewedBy() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(projectMemberService.validateIsMember(project.getId(), creator.getId())).thenReturn(true);
        when(projectMemberService.isPrivileged(project.getId(), creator.getId())).thenReturn(true);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(mock(TaskResponse.class));

        taskService.changeStatus(task.getId(), TaskStatus.COMPLETED, creator);

        assertThat(task.getReviewedBy()).isEqualTo(creator);
    }

    @Test
    void changeStatus_fromTerminalToActive_clearsReviewedBy() {
        task.setStatus(TaskStatus.COMPLETED);
        task.setReviewedBy(creator);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(projectMemberService.validateIsMember(project.getId(), creator.getId())).thenReturn(true);
        when(projectMemberService.isPrivileged(project.getId(), creator.getId())).thenReturn(true);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(mock(TaskResponse.class));

        taskService.changeStatus(task.getId(), TaskStatus.IN_PROGRESS, creator);

        assertThat(task.getReviewedBy()).isNull();
    }

    @Test
    void deleteTask_notPrivileged_throws() {
        var stranger = User.builder().id(99L).build();
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(projectMemberService.isPrivileged(project.getId(), stranger.getId())).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask(task.getId(), stranger))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteTask_privileged_success() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(projectMemberService.isPrivileged(project.getId(), creator.getId())).thenReturn(true);

        taskService.deleteTask(task.getId(), creator);

        verify(taskHistoryService).deleteAllByTaskId(task.getId());
        verify(taskRepository).deleteById(task.getId());
    }

    @Test
    void updateTask_notAllowed_throws() {
        var stranger = User.builder().id(99L).build();
        var request = new TaskUpdateRequest("New Title", "Desc", assignee.getId(), TaskPriority.LOW, LocalDate.now());

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(projectMemberService.isPrivileged(project.getId(), stranger.getId())).thenReturn(false);

        assertThatThrownBy(() -> taskService.updateTask(task.getId(), request, stranger))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void reassignAndHoldTasks_setsAssigneeAndStatus() {
        var newOwner = User.builder().id(3L).build();
        when(taskRepository.findByProjectIdAndAssigneeId(project.getId(), assignee.getId()))
                .thenReturn(List.of(task));

        taskService.reassignAndHoldTasks(project.getId(), assignee.getId(), newOwner);

        assertThat(task.getAssignee()).isEqualTo(newOwner);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.ON_HOLD);
        verify(taskRepository).saveAll(List.of(task));
    }
}
