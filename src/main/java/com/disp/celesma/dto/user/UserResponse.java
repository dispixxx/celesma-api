package com.disp.celesma.dto.user;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String avatarUrl,
        String role,
        LocalDate registrationDate) {
}
