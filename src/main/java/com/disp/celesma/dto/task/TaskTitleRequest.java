package com.disp.celesma.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskTitleRequest(
        @NotBlank(message = "Укажите название")
        @Size(max = 255)
        String title
) {}
