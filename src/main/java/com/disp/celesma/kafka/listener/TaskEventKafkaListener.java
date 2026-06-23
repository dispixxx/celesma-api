package com.disp.celesma.kafka.listener;

import com.disp.celesma.event.task.TaskCreatedEvent;
import com.disp.celesma.event.task.TaskStatusChangedEvent;
import com.disp.celesma.kafka.dto.TaskEventDto;
import com.disp.celesma.kafka.producer.TaskEventProducer;
import com.disp.celesma.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Внутренний обработчик событий, который отправляет события в Kafka послеCommit
 **/
@Component
@Profile("kafka")
@RequiredArgsConstructor
@Slf4j
public class TaskEventKafkaListener {

    private final TaskEventProducer taskEventProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreated(TaskCreatedEvent event) {
        Task task = event.getTask();
        
        TaskEventDto dto = TaskEventDto.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeUsername(task.getAssignee() != null ? task.getAssignee().getUsername() : null)
                .creatorId(task.getCreator().getId())
                .creatorUsername(task.getCreator().getUsername())
                .newStatus(task.getStatus())
                .build();

        taskEventProducer.sendTaskCreated(dto);
        log.info("Task created event sent to Kafka after commit: taskId={}", task.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        Task task = event.getTask();
        
        TaskEventDto dto = TaskEventDto.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeUsername(task.getAssignee() != null ? task.getAssignee().getUsername() : null)
                .creatorId(task.getCreator().getId())
                .creatorUsername(task.getCreator().getUsername())
                .oldStatus(event.getOldStatus())
                .newStatus(event.getNewStatus())
                .build();

        taskEventProducer.sendTaskStatusChanged(dto);
        log.info("Task status changed event sent to Kafka after commit: taskId={}, {} -> {}", 
                task.getId(), event.getOldStatus(), event.getNewStatus());
    }
}
