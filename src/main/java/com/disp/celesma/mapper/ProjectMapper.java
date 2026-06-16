package com.disp.celesma.mapper;

import com.disp.celesma.dto.project.ProjectResponse;
import com.disp.celesma.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;


@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserMapper.class, MemberMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProjectMapper {


    @Mapping(target = "owner", source = "ownerUser")
    @Mapping(target = "members", source = "members")
    ProjectResponse toResponse(Project project);


}