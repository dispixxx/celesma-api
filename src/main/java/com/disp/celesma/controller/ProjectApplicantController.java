package com.disp.celesma.controller;

import com.disp.celesma.dto.applicant.ApplicantResponse;
import com.disp.celesma.dto.member.MemberResponse;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.ProjectMemberService;
import com.disp.celesma.service.interfaces.IProjectService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/applicants")
@AllArgsConstructor
public class ProjectApplicantController {
    private final IProjectService projectService;
    private final ProjectMemberService projectMemberService;

    @GetMapping
    public ResponseEntity<List<ApplicantResponse>> getApplicants(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        var response = projectService.getApplicants(projectId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join")
    public ResponseEntity<ApplicantResponse> joinRequest(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        var response = projectService.addJoinRequest(projectId, principal.getUser());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/separate")
    public ResponseEntity<Void> cancelJoinRequest(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.cancelJoinRequest(projectId, principal.getUser());
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{userId}/accept")
    public ResponseEntity<MemberResponse> acceptApplicant(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        var response = projectMemberService.acceptApplicant(projectId, userId, principal.getUser());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/decline")
    public ResponseEntity<Void> declineApplicant(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectMemberService.declineApplicant(projectId, userId, principal.getUser());
        return ResponseEntity.ok().build();
    }
}
