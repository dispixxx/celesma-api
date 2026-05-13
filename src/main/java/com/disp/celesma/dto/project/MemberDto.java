package com.disp.celesma.dto.project;

import com.disp.celesma.dto.user.UserSummaryDto;
import com.disp.celesma.model.ProjectMember;
import com.disp.celesma.model.enums.ProjectRole;

import java.time.LocalDate;

public record MemberDto(
        Long memberId,
        UserSummaryDto user,
        ProjectRole role,
        LocalDate joinedAt,
        boolean isOwner
) {
    public static MemberDto from(ProjectMember m) {
        boolean isOwner = m.getProject().getOwnerUser().getId().equals(m.getUser().getId());
        return new MemberDto(
                m.getId(),
                UserSummaryDto.from(m.getUser()),
                m.getRole(),
                m.getJoinedAt(),
                isOwner
        );
    }
}
