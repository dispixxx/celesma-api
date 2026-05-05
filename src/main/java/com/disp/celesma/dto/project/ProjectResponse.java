package com.disp.celesma.dto.project;

import com.disp.celesma.dto.user.UserSummaryDto;
import com.disp.celesma.model.Project;
import com.disp.celesma.model.enums.ProjectRole;

import java.util.List;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        UserSummaryDto owner,
        int memberCount,
        ProjectRole currentUserRole,
        boolean isApplicant,
        List<MemberDto> members
) {
    public static ProjectResponse from(Project project, ProjectRole role, boolean isApplicant, List<MemberDto> members) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                UserSummaryDto.from(project.getOwnerUser()),
                project.getMembers().size(),
                role,
                isApplicant,
                members
        );
    }

    public static ProjectResponse from(Project project, ProjectRole role) {
        return from(project, role, false, List.of());
    }
}
