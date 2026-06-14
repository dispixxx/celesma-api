package com.disp.celesma.controller;

import com.disp.celesma.dto.member.MemberResponseDto;
import com.disp.celesma.dto.member.RoleUpdateRequest;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.IProjectMemberService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@AllArgsConstructor
public class ProjectMemberController {

    private final IProjectMemberService projectMemberService;

    @GetMapping
    public ResponseEntity<List<MemberResponseDto>> getMembers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        var response = projectMemberService.getSortedMembersByProjectId(projectId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<MemberResponseDto> updateMemberRole(
            @PathVariable Long projectId, // 0_o
            @PathVariable Long memberId,
            @RequestBody RoleUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        var response = projectMemberService.updateMemberRole(principal.getUser(), request.getProjectId(), memberId, request.getRole());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectMemberService.removeMember(projectId, memberId, principal.getUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{memberId}/exit")
    public ResponseEntity<Void> exitProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long memberId) {
        projectMemberService.exitFromProject(projectId, principal.getUser(), memberId);
        return ResponseEntity.ok().build();
    }
}
