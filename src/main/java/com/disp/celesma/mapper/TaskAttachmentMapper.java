package com.disp.celesma.mapper;


import com.disp.celesma.dto.task.attachment.TaskAttachmentResponse;
import com.disp.celesma.model.TaskAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserMapper.class, TaskMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)

public interface TaskAttachmentMapper {
    TaskAttachmentResponse toResponse(TaskAttachment attachment);
}
