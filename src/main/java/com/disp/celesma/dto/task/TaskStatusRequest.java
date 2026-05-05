package com.disp.celesma.dto.task;

import com.disp.celesma.model.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusRequest(
        @NotNull(message = "Укажите статус") TaskStatus status
) {}
