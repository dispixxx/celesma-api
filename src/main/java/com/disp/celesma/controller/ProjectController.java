package com.disp.celesma.controller;

import com.disp.celesma.dto.project.ProjectCreateRequest;
import com.disp.celesma.dto.project.ProjectResponseDto;
import com.disp.celesma.dto.project.ProjectUpdateRequest;
import com.disp.celesma.mapper.ProjectMapper;
import com.disp.celesma.model.Project;
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
    public ResponseEntity<List<ProjectResponseDto>> getUserProjects(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<ProjectResponseDto> projects = projectService.getUserProjects(principal.getUser());

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> getProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProjectResponseDto project = projectService.getProjectById(projectId);
        return ResponseEntity.ok(project);
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProjectResponseDto created = projectService.createProjectAndSave(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProjectResponseDto project = projectService.updateProjectAndSave(projectId, principal.getUser(), request);

        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.deleteProject(projectId, principal.getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponseDto>> searchProjects(
            @RequestParam String q,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.searchProjects(principal.getUser(), q));
    }

    //    TODO
    /* В PROJECT MEMBER CONTROLLER*/
/*    @Deprecated
    @GetMapping("/{projectId}/applicants")
    public ResponseEntity<?> getApplicants(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getApplicants(projectId));
    }*/

/*    @PostMapping("/{projectId}/applicants/{userId}/accept")
    public ResponseEntity<Void> acceptApplicant(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.acceptApplicant(projectId, userId, principal.getUser());
        return ResponseEntity.ok().build();
    }*/

/*    @DeleteMapping("/{projectId}/applicants/{userId}")
    public ResponseEntity<Void> declineApplicant(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.declineApplicant(projectId, userId, principal.getUser());
        return ResponseEntity.ok().build();
    }*/

/*    @PostMapping("/{projectId}/exit")
    public ResponseEntity<Void> exitProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.exitFromProject(projectId, principal.getUser());
        return ResponseEntity.ok().build();
    }*/
/*
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
    }*/

/*    @PutMapping("/{projectId}/members/{memberId}/role")
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
    }*/
}
