package com.disp.celesma.service.interfaces;

import com.disp.celesma.dto.task.TaskRequest;
import com.disp.celesma.dto.task.TaskResponse;
import com.disp.celesma.model.Task;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.TaskStatus;

import java.util.List;

public interface ITaskService {
    TaskResponse createTask(Long projectId, User creator, TaskRequest request);
    TaskResponse getTaskById(Long taskId);
    Task getTaskEntityById(Long taskId);
    List<TaskResponse> getTasksByProject(Long projectId);
    TaskResponse updateTask(Long taskId, TaskRequest request, User caller);
    TaskResponse changeStatus(Long taskId, TaskStatus newStatus, User caller);
    void deleteTask(Long taskId);
}
