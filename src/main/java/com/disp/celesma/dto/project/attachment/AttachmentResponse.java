package com.disp.celesma.dto.project.attachment;

import com.disp.celesma.dto.project.ProjectPreviewResponse;
import com.disp.celesma.dto.user.UserResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AttachmentResponse(
        Long id,
        String fileUrl,
        String fileName,
        Long fileSize,
        String mimeType,
        ProjectPreviewResponse project,
        UserResponse uploadedBy,

        @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDateTime createdAt
) {
}
