package com.disp.celesma.dto.member;

import com.disp.celesma.dto.user.UserResponse;
import com.disp.celesma.model.enums.ProjectRole;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record MemberResponse(
        Long memberId,
        UserResponse user,
        ProjectRole role,
        @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDateTime joinedAt
) {
}
