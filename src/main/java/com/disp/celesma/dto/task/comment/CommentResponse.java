package com.disp.celesma.dto.task.comment;

import com.disp.celesma.dto.task.TaskResponse;
import com.disp.celesma.dto.user.UserResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * DTO for {@link com.disp.celesma.model.Comment}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CommentResponse(
        Long id,
        String text,
        @JsonIgnore()
        TaskResponse task,
        UserResponse author,
        @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
        LocalDateTime createdAt
) {
}