package com.disp.celesma.dto.project;

import com.disp.celesma.dto.member.MemberResponseDto;
import com.disp.celesma.dto.user.UserResponseDto;

import java.util.List;

public record ProjectResponseDto(
        Long id,
        String name,
        String description,
        UserResponseDto owner,
        List<MemberResponseDto> members
) {
}
