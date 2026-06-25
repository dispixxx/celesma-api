package com.disp.celesma.service.interfaces;

import com.disp.celesma.dto.task.TaskUpdateRequest;
import com.disp.celesma.dto.task.history.TaskHistoryResponse;
import com.disp.celesma.model.Task;
import com.disp.celesma.model.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ITaskHistoryService {

    @Transactional(propagation = Propagation.REQUIRED)
    TaskHistoryResponse record(Task task, User changedBy, String description);

    // Получение истории для контроллера
    @Transactional(readOnly = true)
    List<TaskHistoryResponse> getHistoryByTaskId(Long taskId);

    void deleteAllByTaskId(Long taskId);

    String diffTask(Task task, TaskUpdateRequest request, User assignee);
}
