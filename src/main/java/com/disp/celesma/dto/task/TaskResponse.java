package com.disp.celesma.dto.task;

import com.disp.celesma.dto.user.UserSummaryDto;
import com.disp.celesma.model.Task;
import com.disp.celesma.model.enums.TaskPriority;
import com.disp.celesma.model.enums.TaskStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        UserSummaryDto assignee,
        UserSummaryDto creator,
        UserSummaryDto reviewedBy,
        @JsonFormat(pattern = "dd.MM.yyyy") LocalDate endDate,
        Long projectId,
        TaskStatus status,
        TaskPriority priority
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCreatedAt(),
                UserSummaryDto.from(task.getAssignee()),
                UserSummaryDto.from(task.getCreator()),
                UserSummaryDto.from(task.getReviewedBy()),
                task.getEndDate(),
                task.getProject().getId(),
                task.getStatus(),
                task.getPriority()
        );
    }
}
