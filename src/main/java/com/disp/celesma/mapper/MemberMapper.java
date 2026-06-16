package com.disp.celesma.mapper;

import com.disp.celesma.dto.member.MemberResponse;
import com.disp.celesma.model.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = UserMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface MemberMapper {

    @Mapping(target = "memberId", source = "id")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "joinedAt", source = "joinedAt")
    MemberResponse toResponse(ProjectMember member);

}