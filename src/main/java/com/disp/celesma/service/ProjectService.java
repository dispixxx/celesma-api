package com.disp.celesma.service;

import com.disp.celesma.dto.applicant.ApplicantResponseDto;
import com.disp.celesma.dto.project.ProjectCreateRequest;
import com.disp.celesma.dto.project.ProjectResponseDto;
import com.disp.celesma.dto.project.ProjectUpdateRequest;
import com.disp.celesma.mapper.ProjectMapper;
import com.disp.celesma.mapper.UserMapper;
import com.disp.celesma.model.Project;
import com.disp.celesma.model.ProjectMember;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.ProjectRole;
import com.disp.celesma.repository.ProjectRepository;
import com.disp.celesma.service.interfaces.IProjectMemberService;
import com.disp.celesma.service.interfaces.IProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {

    private final ProjectRepository projectRepository;

    private final IProjectMemberService projectMemberService;

    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;


    @Override
    @Transactional(readOnly = true)
    public Project getProjectEntityById(Long id) {
        return projectRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new EntityNotFoundException("Project %d not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectById(Long id) {
        return projectMapper.toResponse(getProjectEntityById(id));
    }

    @Override
    @Transactional
    public ProjectResponseDto createProjectAndSave(User owner, ProjectCreateRequest request) {
        // 1. Создаём только проект
        var project = Project.builder()
                .name(request.name())
                .description(request.description().trim())
                .ownerUser(owner)
                .members(new HashSet<>())
                .build();

        Project savedProject = projectRepository.save(project);

        // 2. Добавляем владельца как участника через специализированный сервис
        ProjectMember savedMember = projectMemberService.createAndAddMemberToProject(
                savedProject,
                owner,
                ProjectRole.OWNER
        );

        savedProject.getMembers().add(savedMember);
        return projectMapper.toResponse(savedProject);
    }

    /**
     * Для получаения проектов, в которых учавствует пользователь
     **/
    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getUserProjects(User user) {
        return projectMemberService.getAllByUserWithProjectAndOwner(user).stream()
                .map(ProjectMember::getProject)
                .map(projectMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectRole getUserRole(Long projectId, Long userId) {
        return projectMemberService.getUserRole(projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> searchProjects(User user, String query) {
        return projectRepository.findByNameContainingIgnoreCase(query).stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isApplicant(Long projectId, User user) {
        return getProjectEntityById(projectId).getApplicants().contains(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicantResponseDto> getApplicants(Long projectId) {

        return getProjectEntityById(projectId).getApplicants().stream()
                .map(u -> new ApplicantResponseDto(projectId, userMapper.toResponseDto(u), LocalDate.now()))
                .toList();
    }

    @Override
    @Transactional
    public void exitFromProject(Long projectId, User user) {
        var project = getProjectEntityById(projectId);
        if (user.equals(project.getOwnerUser())) {
            throw new IllegalStateException("Владелец не может покинуть проект");
        }
        var member = projectMemberService.getProjectMemberByProjectIdAndUserId(projectId, user.getId());
        project.getMembers().remove(member);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public ApplicantResponseDto addJoinRequest(Long projectId, User user) {
        var project = getProjectEntityById(projectId);
        if (projectMemberService.isMember(projectId, user.getId())) {
            throw new IllegalStateException("Вы уже участник проекта");
        }
        if (project.getApplicants().contains(user)) {
            throw new IllegalStateException("Заявка уже подана");
        }
        project.getApplicants().add(user);
        projectRepository.save(project);
        return new ApplicantResponseDto(projectId, userMapper.toResponseDto(user), LocalDate.now());

    }

    @Override
    @Transactional
    public void cancelJoinRequest(Long projectId, User user) {
        var project = getProjectEntityById(projectId);
        project.getApplicants().remove(user);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public ProjectResponseDto updateProjectAndSave(Long projectId, User caller, ProjectUpdateRequest request) {
        var project = getProjectEntityById(projectId);
        if (!projectMemberService.isPrivileged(projectId, caller.getId())) {
            throw new IllegalStateException("Нет прав для редактирования проекта");
        }
        project.setName(request.name());
        project.setDescription(request.description().trim());
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, User caller) {
        if (projectMemberService.getUserRole(projectId, caller.getId()) != ProjectRole.OWNER) {
            throw new IllegalStateException("Удалить проект может только OWNER");
        }
        projectRepository.deleteById(projectId);
    }
}
