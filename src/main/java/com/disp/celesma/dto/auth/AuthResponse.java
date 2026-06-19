package com.disp.celesma.dto.auth;

public record AuthResponse(
        String token,
        String username,
        String email,
        String role
) {
}
