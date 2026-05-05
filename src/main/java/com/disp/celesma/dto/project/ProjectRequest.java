package com.disp.celesma.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank(message = "Название не может быть пустым")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Описание не может быть пустым")
        @Size(max = 500)
        String description
) {}
