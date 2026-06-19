package com.disp.celesma.dto.task;

import com.disp.celesma.model.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequest(
        @NotNull(message = "Укажите статус") TaskStatus status
) {}
