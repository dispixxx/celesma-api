package com.disp.celesma.kafka.dto;

import com.disp.celesma.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEventDto {

    private Long taskId;
    private String taskTitle;
    private Long projectId;
    private String projectName;
    private Long assigneeId;
    private String assigneeUsername;
    private Long creatorId;
    private String creatorUsername;
    private TaskStatus oldStatus;
    private TaskStatus newStatus;
    private String eventType; // CREATED, STATUS_CHANGED
    private LocalDateTime occurredAt;
}
