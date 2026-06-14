package com.disp.celesma.dto.member;

import com.disp.celesma.dto.user.UserResponseDto;
import com.disp.celesma.model.enums.ProjectRole;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record MemberResponseDto(
        Long memberId,
        UserResponseDto user,
        ProjectRole role,
        @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDateTime joinedAt
) {
}
