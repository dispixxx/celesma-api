package com.disp.celesma.service;

import com.disp.celesma.dto.project.ProjectCreateRequest;
import com.disp.celesma.dto.project.ProjectResponse;
import com.disp.celesma.dto.project.ProjectUpdateRequest;
import com.disp.celesma.mapper.ProjectAttachmentMapper;
import com.disp.celesma.mapper.ProjectMapper;
import com.disp.celesma.mapper.UserMapper;
import com.disp.celesma.model.Project;
import com.disp.celesma.model.ProjectMember;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.ProjectRole;
import com.disp.celesma.repository.ProjectAttachmentRepository;
import com.disp.celesma.repository.ProjectRepository;
import com.disp.celesma.s3.service.interfaces.IStorageService;
import com.disp.celesma.service.interfaces.IProjectMemberService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ProjectRepository projectRepository;
    @Mock ProjectAttachmentRepository attachmentRepository;
    @Mock IProjectMemberService projectMemberService;
    @Mock IStorageService storageService;
    @Mock ProjectMapper projectMapper;
    @Mock UserMapper userMapper;
    @Mock ProjectAttachmentMapper projectAttachmentMapper;

    @InjectMocks ProjectService projectService;

    private User owner;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).username("owner").build();
        project = Project.builder()
                .id(10L)
                .name("My Project")
                .description("desc")
                .ownerUser(owner)
                .members(new HashSet<>())
                .applicants(new HashSet<>())
                .build();
    }

    @Test
    void createProject_success() {
        var request = new ProjectCreateRequest("My Project", "desc");
        var member = mock(ProjectMember.class);
        var response = mock(ProjectResponse.class);

        when(projectRepository.save(any())).thenReturn(project);
        when(projectMemberService.createAndAddMemberToProject(project, owner, ProjectRole.OWNER)).thenReturn(member);
        when(projectMapper.toResponse(project)).thenReturn(response);

        var result = projectService.createProjectAndSave(owner, request);

        assertThat(result).isEqualTo(response);
        verify(projectMemberService).createAndAddMemberToProject(project, owner, ProjectRole.OWNER);
    }

    @Test
    void getProjectById_notFound_throws() {
        when(projectRepository.findByIdWithOwner(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateProject_notPrivileged_throws() {
        var caller = User.builder().id(2L).build();
        var request = new ProjectUpdateRequest("New Name", "New Desc");

        when(projectRepository.findByIdWithOwner(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberService.isPrivileged(project.getId(), caller.getId())).thenReturn(false);

        assertThatThrownBy(() -> projectService.updateProjectAndSave(project.getId(), caller, request))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateProject_privileged_success() {
        var request = new ProjectUpdateRequest("New Name", "New Desc");
        var response = mock(ProjectResponse.class);

        when(projectRepository.findByIdWithOwner(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberService.isPrivileged(project.getId(), owner.getId())).thenReturn(true);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(response);

        var result = projectService.updateProjectAndSave(project.getId(), owner, request);

        assertThat(result).isEqualTo(response);
        assertThat(project.getName()).isEqualTo("New Name");
    }

    @Test
    void deleteProject_notOwner_throws() {
        var caller = User.builder().id(2L).build();
        when(projectMemberService.getUserRole(project.getId(), caller.getId())).thenReturn(ProjectRole.ADMIN);

        assertThatThrownBy(() -> projectService.deleteProject(project.getId(), caller))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deleteProject_owner_success() {
        when(projectMemberService.getUserRole(project.getId(), owner.getId())).thenReturn(ProjectRole.OWNER);

        projectService.deleteProject(project.getId(), owner);

        verify(projectRepository).deleteById(project.getId());
    }

    @Test
    void addJoinRequest_alreadyMember_throws() {
        when(projectRepository.findByIdWithOwner(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberService.isMember(project.getId(), owner.getId())).thenReturn(true);

        assertThatThrownBy(() -> projectService.addJoinRequest(project.getId(), owner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("уже участник");
    }

    @Test
    void addJoinRequest_alreadyApplied_throws() {
        project.getApplicants().add(owner);
        when(projectRepository.findByIdWithOwner(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberService.isMember(project.getId(), owner.getId())).thenReturn(false);

        assertThatThrownBy(() -> projectService.addJoinRequest(project.getId(), owner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("уже подана");
    }

    @Test
    void exitFromProject_owner_throws() {
        when(projectRepository.findByIdWithOwner(project.getId())).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.exitFromProject(project.getId(), owner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Владелец");
    }

    @Test
    void getUserProjects_returnsMappedList() {
        var member = mock(ProjectMember.class);
        var response = mock(ProjectResponse.class);
        when(member.getProject()).thenReturn(project);
        when(projectMemberService.getAllByUserWithProjectAndOwner(owner)).thenReturn(List.of(member));
        when(projectMapper.toResponse(project)).thenReturn(response);

        var result = projectService.getUserProjects(owner);

        assertThat(result).containsExactly(response);
    }
}
