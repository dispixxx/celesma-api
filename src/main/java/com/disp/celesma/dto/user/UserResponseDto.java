package com.disp.celesma.dto.user;

import com.disp.celesma.model.User;

import java.time.LocalDate;

public record UserResponseDto(
        Long id,
        String username,
        String firstName,
        String lastName,
        String avatarUrl,
        String role,
        LocalDate registrationDate) {
}
