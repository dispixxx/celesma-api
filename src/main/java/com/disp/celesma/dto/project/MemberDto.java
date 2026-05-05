package com.disp.celesma.dto.project;

import com.disp.celesma.dto.user.UserSummaryDto;
import com.disp.celesma.model.ProjectMember;
import com.disp.celesma.model.enums.ProjectRole;

import java.time.LocalDate;

public record MemberDto(
        Long memberId,
        UserSummaryDto user,
        ProjectRole role,
        LocalDate joinedAt
) {
    public static MemberDto from(ProjectMember m) {
        return new MemberDto(
                m.getId(),
                UserSummaryDto.from(m.getUser()),
                m.getRole(),
                m.getJoinedAt()
        );
    }
}
