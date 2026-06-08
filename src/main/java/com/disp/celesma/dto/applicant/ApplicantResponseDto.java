package com.disp.celesma.dto.applicant;

import com.disp.celesma.dto.user.UserResponseDto;

import java.time.LocalDate;

public record ApplicantResponseDto(
        Long projectId,
        UserResponseDto user,
        LocalDate requestAt

) {
}
