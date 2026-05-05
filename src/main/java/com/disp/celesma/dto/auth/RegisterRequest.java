package com.disp.celesma.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "Некорректный email") String email,
        @Size(min = 4, message = "Пароль минимум 4 символа") String password,
        @NotBlank(message = "Укажите имя") String firstName,
        @NotBlank(message = "Укажите фамилию") String lastName,
        @NotBlank @Size(min = 3, max = 20) String nickname
) {}
