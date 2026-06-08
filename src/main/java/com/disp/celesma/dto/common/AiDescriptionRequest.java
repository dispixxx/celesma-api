package com.disp.celesma.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiDescriptionRequest {

    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 2000, message = "Описание слишком длинное")
    private String description;

    @NotBlank(message = "Укажите действие")
    private String action;
}