package com.disp.celesma.dto.task.history;

import com.disp.celesma.dto.user.UserResponse;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record TaskHistoryResponse(
        Long id,
        UserResponse changedBy,
        String description,
        @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
        LocalDateTime changedAt
) {}
