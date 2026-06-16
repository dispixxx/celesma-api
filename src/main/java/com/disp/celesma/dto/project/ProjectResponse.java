package com.disp.celesma.dto.project;

import com.disp.celesma.dto.member.MemberResponse;
import com.disp.celesma.dto.user.UserResponse;

import java.util.List;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        UserResponse owner,
        List<MemberResponse> members
) {
}
