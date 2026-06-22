package com.disp.celesma.controller;

import com.disp.celesma.dto.task.comment.CommentCreateRequest;
import com.disp.celesma.dto.task.comment.CommentResponse;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.ICommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks/{taskId}/comments")
@Tag(name = "Comment Controller", description = "API для управления комментариями к задачам")
public class CommentController {
    private final ICommentService commentService;

    @PostMapping
    @Operation(summary = "Добавить комментарий к задаче")
    @PreAuthorize("@projectSecurity.isMemberByTask(#taskId, principal)")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long taskId,
            @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        // Создаём комментарий
        CommentResponse savedComment = commentService.createAndSave(request, principal.getUser(), taskId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedComment);
    }

    @GetMapping
    @Operation(summary = "Получить все комментарии задачи")
    @PreAuthorize("@projectSecurity.isMemberByTask(#taskId, principal)")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        var comments = commentService.getByTask(taskId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Получить комментарий по ID")
    @PreAuthorize("@projectSecurity.isMemberByTask(#taskId, principal)")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable Long commentId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        var comment = commentService.getCommentById(commentId);
        return ResponseEntity.ok(comment);
    }
}
