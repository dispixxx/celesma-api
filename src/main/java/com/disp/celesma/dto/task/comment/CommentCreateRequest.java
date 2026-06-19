package com.disp.celesma.dto.task.comment;

import jakarta.validation.constraints.NotNull;

public record CommentCreateRequest(
        @NotNull(message = "Не может быть пустым") String text
) {
}
