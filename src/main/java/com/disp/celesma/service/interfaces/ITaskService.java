package com.disp.celesma.service.interfaces;

import com.disp.celesma.dto.task.TaskCreateRequest;
import com.disp.celesma.dto.task.TaskResponse;
import com.disp.celesma.dto.task.TaskUpdateRequest;
import com.disp.celesma.model.Task;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.TaskStatus;

import java.util.List;

public interface ITaskService {
    TaskResponse createTaskAndSave(Long projectId, User creator, TaskCreateRequest request);

    TaskResponse getTaskById(Long taskId);

    Task getTaskEntityById(Long taskId);

    List<TaskResponse> getTasksByProject(Long projectId);

    TaskResponse updateTask(Long taskId, TaskUpdateRequest request, User caller);

    TaskResponse changeStatus(Long taskId, TaskStatus newStatus, User caller);

    TaskResponse changeStatus(Long taskId, TaskStatus newStatus, String callerUsername);

    void deleteTask(Long taskId, User caller);

    void reassignAndHoldTasks(Long projectId, Long fromUserId, User toUser);
}
