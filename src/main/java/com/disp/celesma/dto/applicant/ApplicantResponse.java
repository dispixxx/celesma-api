package com.disp.celesma.dto.applicant;

import com.disp.celesma.dto.user.UserResponse;

import java.time.LocalDate;

public record ApplicantResponse(
        Long projectId,
        UserResponse user,
        LocalDate requestAt

) {
}
