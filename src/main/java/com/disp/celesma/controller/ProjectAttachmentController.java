package com.disp.celesma.controller;

import com.disp.celesma.dto.project.attachment.AttachmentResponse;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/attachments")
@RequiredArgsConstructor
public class ProjectAttachmentController {

    private final IProjectService projectService;

    @GetMapping
    @PreAuthorize("@projectSecurity.isMember(#projectId, principal)")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getAttachments(projectId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@projectSecurity.isMember(#projectId, principal)")
    public ResponseEntity<AttachmentResponse> upload(
            @PathVariable Long projectId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                projectService.uploadAttachment(projectId, file, principal.getUser())
        );
    }

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("@projectSecurity.isMember(#projectId, principal)")
    public ResponseEntity<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.deleteAttachment(projectId, attachmentId, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}