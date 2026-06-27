package com.disp.celesma.mapper;


import com.disp.celesma.dto.project.attachment.ProjectAttachmentResponse;
import com.disp.celesma.model.ProjectAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserMapper.class, ProjectMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)

public interface ProjectAttachmentMapper {
    @Mapping(target = "project", source = "project", qualifiedByName = "toPreview")
    ProjectAttachmentResponse toResponse(ProjectAttachment attachment);
}
