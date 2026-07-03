package com.disp.celesma.service;

import com.disp.celesma.mapper.MemberMapper;
import com.disp.celesma.model.Project;
import com.disp.celesma.model.ProjectMember;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.ProjectRole;
import com.disp.celesma.repository.ProjectMemberRepository;
import com.disp.celesma.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock ProjectMemberRepository projectMemberRepository;
    @Mock ProjectRepository projectRepository;
    @Mock MemberMapper memberMapper;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks ProjectMemberService projectMemberService;

    private User owner;
    private User member;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).username("owner").build();
        member = User.builder().id(2L).username("member").build();
        project = Project.builder().id(10L).name("Project").ownerUser(owner).members(new HashSet<>()).build();
    }

    @Test
    void isPrivileged_owner_returnsTrue() {
        when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(ProjectMember.builder().role(ProjectRole.OWNER).build()));

        assertThat(projectMemberService.isPrivileged(10L, 1L)).isTrue();
    }

    @Test
    void isPrivileged_member_returnsFalse() {
        when(projectMemberRepository.findByProjectIdAndUserId(10L, 2L))
                .thenReturn(Optional.of(ProjectMember.builder().role(ProjectRole.MEMBER).build()));

        assertThat(projectMemberService.isPrivileged(10L, 2L)).isFalse();
    }

    @Test
    void isPrivileged_notFound_returnsFalse() {
        when(projectMemberRepository.findByProjectIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

        assertThat(projectMemberService.isPrivileged(10L, 99L)).isFalse();
    }

    @Test
    void validateIsMember_notMember_throws() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(10L, 99L)).thenReturn(false);

        assertThatThrownBy(() -> projectMemberService.validateIsMember(10L, 99L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void validateIsMember_isMember_returnsTrue() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(true);

        assertThat(projectMemberService.validateIsMember(10L, 2L)).isTrue();
    }

    @Test
    void updateMemberRole_ownerRole_throws() {
        var ownerMember = ProjectMember.builder().id(5L).project(project).role(ProjectRole.OWNER).user(owner).build();

        when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(ProjectMember.builder().role(ProjectRole.OWNER).build()));
        when(projectMemberRepository.findById(5L)).thenReturn(Optional.of(ownerMember));

        assertThatThrownBy(() -> projectMemberService.updateMemberRole(owner, 10L, 5L, ProjectRole.ADMIN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("владельца");
    }

    @Test
    void updateMemberRole_moderatorAssignsAdmin_throws() {
        var moderator = User.builder().id(3L).build();
        var targetMember = ProjectMember.builder().id(6L).project(project).role(ProjectRole.MEMBER).user(member).build();

        when(projectMemberRepository.findByProjectIdAndUserId(10L, 3L))
                .thenReturn(Optional.of(ProjectMember.builder().role(ProjectRole.MODERATOR).build()));
        when(projectMemberRepository.findById(6L)).thenReturn(Optional.of(targetMember));

        assertThatThrownBy(() -> projectMemberService.updateMemberRole(moderator, 10L, 6L, ProjectRole.ADMIN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Модератор");
    }

    @Test
    void removeMember_ownerRole_throws() {
        var ownerMember = ProjectMember.builder().id(5L).project(project).role(ProjectRole.OWNER).user(owner).build();

        when(projectMemberRepository.findByProjectIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(ProjectMember.builder().role(ProjectRole.OWNER).build()));
        when(projectMemberRepository.findById(5L)).thenReturn(Optional.of(ownerMember));

        assertThatThrownBy(() -> projectMemberService.removeMember(10L, 5L, owner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("владельца");
    }

    @Test
    void exitFromProject_ownerExits_throws() {
        var ownerMember = ProjectMember.builder().id(5L).project(project).role(ProjectRole.OWNER).user(owner).build();
        when(projectMemberRepository.findById(5L)).thenReturn(Optional.of(ownerMember));

        assertThatThrownBy(() -> projectMemberService.exitFromProject(10L, owner, 5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ВЛАДЕЛЕЦ");
    }

    @Test
    void createAndAddMemberToProject_savesAndReturns() {
        var saved = ProjectMember.builder().id(7L).project(project).user(member).role(ProjectRole.MEMBER).build();
        when(projectMemberRepository.save(any())).thenReturn(saved);

        var result = projectMemberService.createAndAddMemberToProject(project, member, ProjectRole.MEMBER);

        assertThat(result.getRole()).isEqualTo(ProjectRole.MEMBER);
        assertThat(result.getUser()).isEqualTo(member);
    }
}
