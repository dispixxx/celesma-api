package com.disp.celesma.dto.task;

import com.disp.celesma.dto.user.UserResponse;
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
        UserResponse assignee,
        UserResponse creator,
        UserResponse reviewedBy,
        @JsonFormat(pattern = "dd.MM.yyyy") LocalDate endDate,
        Long projectId,
        TaskStatus status,
        TaskPriority priority
) {}
