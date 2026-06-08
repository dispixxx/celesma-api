package com.disp.celesma.dto.task;

import com.disp.celesma.dto.member.MemberResponseDto;
import com.disp.celesma.dto.user.UserResponseDto;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.TaskPriority;
import com.disp.celesma.model.enums.TaskStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        UserResponseDto assignee,
        UserResponseDto creator,
        UserResponseDto reviewedBy,
        @JsonFormat(pattern = "dd.MM.yyyy") LocalDate endDate,
        Long projectId,
        TaskStatus status,
        TaskPriority priority
) {
}
