package com.disp.celesma.mapper;

import com.disp.celesma.dto.project.ProjectResponse;
import com.disp.celesma.model.Project;
import com.disp.celesma.dto.project.ProjectPreviewResponse;
import org.mapstruct.*;


@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserMapper.class, MemberMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProjectMapper {


    @Mapping(target = "owner", source = "ownerUser")
    @Mapping(target = "members", source = "members")
    ProjectResponse toResponse(Project project);

    @Named("toPreview")
    ProjectPreviewResponse toProjectPreviewResponse(Project project);
}