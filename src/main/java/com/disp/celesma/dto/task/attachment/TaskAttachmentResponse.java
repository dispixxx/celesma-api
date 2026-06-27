package com.disp.celesma.dto.task.attachment;


import com.disp.celesma.dto.task.TaskResponse;
import com.disp.celesma.dto.user.UserResponse;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record TaskAttachmentResponse(
        Long id,
        TaskResponse task,
        UserResponse uploadedBy,
        String fileUrl,
        String fileName,
        Long fileSize,
        String mimeType,
        @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDateTime createdAt) {
}
