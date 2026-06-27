package com.disp.celesma.controller;

import com.disp.celesma.dto.task.attachment.TaskAttachmentResponse;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks/{taskId}/attachments")
@RequiredArgsConstructor
public class TaskAttachmentController {

    private final ITaskService taskService;

    @GetMapping
    @PreAuthorize("@projectSecurity.isMember(#projectId, principal)")
    public ResponseEntity<List<TaskAttachmentResponse>> getAttachments(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getAttachments(taskId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@projectSecurity.isMember(#projectId, principal)")
    public ResponseEntity<TaskAttachmentResponse> upload(
            @PathVariable String projectId,
            @PathVariable Long taskId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.uploadAttachment(taskId, file, principal.getUser()));
    }

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("@projectSecurity.isMember(#projectId, principal)")
    public ResponseEntity<Void> delete(
            @PathVariable String projectId,
            @PathVariable Long taskId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        taskService.deleteAttachment(taskId, attachmentId, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}