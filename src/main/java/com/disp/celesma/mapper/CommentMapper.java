package com.disp.celesma.mapper;


import com.disp.celesma.dto.task.comment.CommentResponse;
import com.disp.celesma.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserMapper.class, TaskMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CommentMapper {
    CommentResponse toResponse(Comment comment);
}
