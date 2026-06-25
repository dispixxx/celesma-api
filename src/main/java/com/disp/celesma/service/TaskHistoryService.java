package com.disp.celesma.service;

import com.disp.celesma.dto.task.TaskUpdateRequest;
import com.disp.celesma.dto.task.history.TaskHistoryResponse;
import com.disp.celesma.mapper.TaskHistoryMapper;
import com.disp.celesma.model.Task;
import com.disp.celesma.model.TaskHistory;
import com.disp.celesma.model.User;
import com.disp.celesma.repository.TaskHistoryRepository;
import com.disp.celesma.service.interfaces.ITaskHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskHistoryService implements ITaskHistoryService {

    private final TaskHistoryRepository taskHistoryRepository;

    private final TaskHistoryMapper taskHistoryMapper;


    public String diffTask(Task task, TaskUpdateRequest request, User newAssignee) {
        var changes = new StringBuilder();

        if (!Objects.equals(task.getTitle(), request.title()))
            changes.append("Название: '%s' → '%s'; ".formatted(task.getTitle(), request.title()));

        if (!Objects.equals(task.getDescription(), request.description()))
            changes.append("Описание изменено; ");

        if (task.getPriority() != request.priority())
            changes.append("Приоритет: %s → %s; ".formatted(task.getPriority(), request.priority()));

        if (!Objects.equals(task.getAssignee(), newAssignee)) {
            var oldName = task.getAssignee() != null ? task.getAssignee().getUsername() : "—";
            changes.append("Исполнитель: '%s' → '%s'; ".formatted(oldName, newAssignee.getUsername()));
        }

        if (!Objects.equals(task.getEndDate(), request.endDate())) {
            var oldDate = task.getEndDate() != null ? task.getEndDate().toString() : "—";
            var newDate = request.endDate() != null ? request.endDate().toString() : "—";
            changes.append("Срок: %s → %s; ".formatted(oldDate, newDate));
        }

        return changes.toString().replaceAll("; $", "");
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public TaskHistoryResponse record(Task task, User changedBy, String description) {
        var history = taskHistoryRepository.save(TaskHistory.builder()
                .task(task)
                .changedBy(changedBy)
                .description(description)
                .changedAt(LocalDateTime.now())
                .build());

        return taskHistoryMapper.toResponse(history);
    }

    // Получение истории для контроллера
    @Override
    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> getHistoryByTaskId(Long taskId) {
        return taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId).stream()
                .map(taskHistoryMapper::toResponse)
                .toList();

    }


    @Override
    public void deleteAllByTaskId(Long taskId) {
        taskHistoryRepository.deleteAllByTaskId(taskId);
    }
}
