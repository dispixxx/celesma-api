package com.disp.celesma.mapper;

import com.disp.celesma.dto.task.TaskResponse;
import com.disp.celesma.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {MemberMapper.class, UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TaskMapper {

    /**
     * Task → TaskResponse.
     * projectId маппится из project.id, assignee/creator/reviewedBy — через MemberMapper.userToMemberResponse.
     */
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "assignee", source = "assignee")
    @Mapping(target = "creator", source = "creator")
    @Mapping(target = "reviewedBy", source = "reviewedBy")
    TaskResponse toResponse(Task task);

}