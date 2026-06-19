package com.disp.celesma.controller;

import com.disp.celesma.dto.project.ProjectCreateRequest;
import com.disp.celesma.dto.project.ProjectResponse;
import com.disp.celesma.dto.project.ProjectUpdateRequest;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.IProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
        List<ProjectResponse> projects = projectService.getUserProjects(principal.getUser());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProjectResponse project = projectService.getProjectById(projectId);
        return ResponseEntity.ok(project);
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProjectResponse created = projectService.createProjectAndSave(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("@projectSecurity.isMember(#projectId, principal)")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProjectResponse project = projectService.updateProjectAndSave(projectId, principal.getUser(), request);

        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("@projectSecurity.isMember(#projectId, principal)")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.deleteProject(projectId, principal.getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponse>> searchProjects(
            @RequestParam String q,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.searchProjects(principal.getUser(), q));
    }
}
