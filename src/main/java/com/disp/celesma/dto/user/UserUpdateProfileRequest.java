package com.disp.celesma.dto.user;

public record UserUpdateProfileRequest(
        String firstName,
        String lastName,
        String bio
) {

}
