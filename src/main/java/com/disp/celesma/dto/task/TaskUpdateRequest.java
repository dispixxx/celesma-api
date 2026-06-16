package com.disp.celesma.dto.task;

import com.disp.celesma.model.enums.TaskPriority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskUpdateRequest(
        @NotBlank(message = "Укажите название")
        @Size(max = 255)
        String title,

        @NotBlank(message = "Укажите описание")
        @Size(max = 1200)
        String description,

        @NotNull(message = "Укажите исполнителя")
        Long assigneeId,

        @NotNull(message = "Укажите приоритет")
        TaskPriority priority,

        @NotNull(message = "Укажите дату")
        @Future(message = "Дата должна быть в будущем")
        LocalDate endDate
) {}
