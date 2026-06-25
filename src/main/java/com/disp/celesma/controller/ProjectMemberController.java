package com.disp.celesma.controller;

import com.disp.celesma.dto.member.MemberResponse;
import com.disp.celesma.dto.member.RoleUpdateRequest;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.IProjectMemberService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@AllArgsConstructor
public class ProjectMemberController {

    private final IProjectMemberService projectMemberService;

    @GetMapping
    @PreAuthorize("@projectSecurity.isMember(#projectId, principal)")
    public ResponseEntity<List<MemberResponse>> getMembers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        var response = projectMemberService.getSortedMembersByProjectId(projectId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{memberId}")
    @PreAuthorize("@projectSecurity.isMember(#projectId, principal)")
    public ResponseEntity<MemberResponse> updateMemberRole(
            @PathVariable Long projectId, // 0_o
            @PathVariable Long memberId,
            @RequestBody RoleUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        var response = projectMemberService.updateMemberRole(principal.getUser(), request.getProjectId(), memberId, request.getRole());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{memberId}")
    @PreAuthorize("@projectSecurity.isMember(#projectId, principal)")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectMemberService.removeMember(projectId, memberId, principal.getUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{memberId}/exit")
    @PreAuthorize("@projectSecurity.isMember(#projectId, principal)")
    public ResponseEntity<Void> exitProject(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectMemberService.exitFromProject(projectId, principal.getUser(), memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{memberId}/transfer-ownership")
    @PreAuthorize("@projectSecurity.isOwner(#projectId, principal)")
    public ResponseEntity<Void> transferOwnership(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectMemberService.transferOwnership(projectId, principal.getUser(), memberId);
        return ResponseEntity.ok().build();
    }
}
