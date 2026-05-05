package com.disp.celesma.dto.project;

import com.disp.celesma.dto.user.UserSummaryDto;
import com.disp.celesma.model.ProjectMember;
import com.disp.celesma.model.enums.ProjectRole;

import java.time.LocalDate;

public record MemberResponse(
        Long memberId,
        UserSummaryDto user,
        ProjectRole role,
        LocalDate joinedAt
) {
    public static MemberResponse from(ProjectMember member) {
        return new MemberResponse(
                member.getId(),
                UserSummaryDto.from(member.getUser()),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}
