package com.disp.celesma.controller;

import com.disp.celesma.dto.project.ProjectRequest;
import com.disp.celesma.dto.project.ProjectResponse;
import com.disp.celesma.dto.project.RoleUpdateRequest;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.IProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final IProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getUserProjects(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.getUserProjects(principal.getUser()));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.getProjectResponse(projectId, principal.getUser()));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        var project = projectService.createProject(principal.getUser(), request);
        var response = ProjectResponse.from(project, projectService.getUserRole(project.getId(), principal.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        var project = projectService.updateProject(projectId, principal.getUser(), request);
        var role = projectService.getUserRole(projectId, principal.getId());
        return ResponseEntity.ok(ProjectResponse.from(project, role));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.deleteProject(projectId, principal.getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/applicants")
    public ResponseEntity<?> getApplicants(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getApplicants(projectId));
    }

    @PostMapping("/{projectId}/applicants/{userId}/accept")
    public ResponseEntity<Void> acceptApplicant(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.acceptApplicant(projectId, userId, principal.getUser());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}/applicants/{userId}")
    public ResponseEntity<Void> declineApplicant(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.declineApplicant(projectId, userId, principal.getUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponse>> searchProjects(
            @RequestParam String q,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.searchProjects(principal.getUser(), q));
    }

    @PostMapping("/{projectId}/exit")
    public ResponseEntity<Void> exitProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.exitFromProject(projectId, principal.getUser());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{projectId}/join")
    public ResponseEntity<Void> joinRequest(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.addJoinRequest(projectId, principal.getUser());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}/join")
    public ResponseEntity<Void> cancelJoinRequest(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.cancelJoinRequest(projectId, principal.getUser());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{projectId}/members/{memberId}/role")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestBody RoleUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.updateMemberRole(projectId, memberId, request.getRole(), principal.getUser());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.removeMember(projectId, memberId, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
