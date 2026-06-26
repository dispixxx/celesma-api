package com.disp.celesma.mapper;


import com.disp.celesma.dto.project.attachment.AttachmentResponse;
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

public interface AttachmentMapper {
    @Mapping(target = "project", source = "project", qualifiedByName = "toPreview")
    AttachmentResponse toResponse(ProjectAttachment attachment);
}
