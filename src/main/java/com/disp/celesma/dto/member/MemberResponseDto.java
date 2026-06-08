package com.disp.celesma.dto.member;

import com.disp.celesma.dto.user.UserResponseDto;
import com.disp.celesma.model.enums.ProjectRole;

import java.time.LocalDateTime;

public record MemberResponseDto(
        Long memberId,
        UserResponseDto user,
        ProjectRole role,
        LocalDateTime joinedAt
) {
}
