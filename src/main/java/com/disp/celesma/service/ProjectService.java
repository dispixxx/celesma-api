package com.disp.celesma.service;

import com.disp.celesma.dto.project.MemberDto;
import com.disp.celesma.dto.project.ProjectRequest;
import com.disp.celesma.dto.project.ProjectResponse;
import com.disp.celesma.model.Project;
import com.disp.celesma.model.ProjectMember;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.ProjectRole;
import com.disp.celesma.repository.ProjectMemberRepository;
import com.disp.celesma.repository.ProjectRepository;
import com.disp.celesma.service.interfaces.IProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    private static final Map<ProjectRole, Integer> ROLE_ORDER = Map.of(
            ProjectRole.ADMIN, 1,
            ProjectRole.MODERATOR, 2,
            ProjectRole.MEMBER, 3,
            ProjectRole.VIEWER, 4
    );

    // Сортировка участников: по роли, затем по дате вступления
    private List<MemberDto> getSortedMembers(Project project) {
        return project.getMembers().stream()
                .sorted(Comparator
                        .<ProjectMember, Integer>comparing(m -> ROLE_ORDER.getOrDefault(m.getRole(), 99))
                        .thenComparing(m -> m.getJoinedAt() != null ? m.getJoinedAt() : LocalDate.MIN))
                .map(MemberDto::from)
                .toList();
    }

    @Override
    @Transactional
    public Project createProject(User owner, ProjectRequest request) {
        var project = Project.builder()
                .name(request.name())
                .description(request.description().trim())
                .ownerUser(owner)
                .members(new HashSet<>())
                .build();

        var member = ProjectMember.builder()
                .project(project)
                .user(owner)
                .role(ProjectRole.ADMIN)
                .joinedAt(LocalDate.now())
                .build();

        project.getMembers().add(member);
        return projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        return projectRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new EntityNotFoundException("Project %d not found".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjects(User user) {
        return projectMemberRepository.findByUser(user).stream()
                .map(m -> ProjectResponse.from(m.getProject(), m.getRole()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectRole getUserRole(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .map(ProjectMember::getRole)
                .orElse(ProjectRole.VIEWER);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserMember(Long projectId, Long userId) {
        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> searchProjects(User user, String query) {
        return projectRepository.findByNameContainingIgnoreCase(query).stream()
                .map(p -> ProjectResponse.from(p, getUserRole(p.getId(), user.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isApplicant(Long projectId, User user) {
        return getProjectById(projectId).getApplicants().contains(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getApplicants(Long projectId) {
        return new ArrayList<>(getProjectById(projectId).getApplicants());
    }

    @Override
    @Transactional
    public void exitFromProject(Long projectId, User user) {
        var project = getProjectById(projectId);
        if (user.equals(project.getOwnerUser())) {
            throw new IllegalStateException("Владелец не может покинуть проект");
        }
        var member = projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Вы не участник проекта"));
        project.getMembers().remove(member);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void addJoinRequest(Long projectId, User user) {
        var project = getProjectById(projectId);
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new IllegalStateException("Вы уже участник проекта");
        }
        if (project.getApplicants().contains(user)) {
            throw new IllegalStateException("Заявка уже подана");
        }
        project.getApplicants().add(user);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void cancelJoinRequest(Long projectId, User user) {
        var project = getProjectById(projectId);
        project.getApplicants().remove(user);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project updateProject(Long projectId, User caller, ProjectRequest request) {
        var project = getProjectById(projectId);
        var role = getUserRole(projectId, caller.getId());
        if (role != ProjectRole.ADMIN && role != ProjectRole.MODERATOR) {
            throw new IllegalStateException("Нет прав для редактирования проекта");
        }
        project.setName(request.name());
        project.setDescription(request.description().trim());
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, User caller) {
        var role = getUserRole(projectId, caller.getId());
        if (role != ProjectRole.ADMIN) {
            throw new IllegalStateException("Удалить проект может только ADMIN");
        }
        projectRepository.deleteById(projectId);
    }

    @Override
    @Transactional
    public void acceptApplicant(Long projectId, Long userId, User caller) {
        var role = getUserRole(projectId, caller.getId());
        if (role != ProjectRole.ADMIN && role != ProjectRole.MODERATOR) {
            throw new IllegalStateException("Нет прав для принятия заявок");
        }
        var project = getProjectById(projectId);
        var applicant = project.getApplicants().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена"));

        project.getApplicants().remove(applicant);
        project.getMembers().add(ProjectMember.builder()
                .project(project)
                .user(applicant)
                .role(ProjectRole.MEMBER)
                .joinedAt(LocalDate.now())
                .build());
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void declineApplicant(Long projectId, Long userId, User caller) {
        var role = getUserRole(projectId, caller.getId());
        if (role != ProjectRole.ADMIN && role != ProjectRole.MODERATOR) {
            throw new IllegalStateException("Нет прав для отклонения заявок");
        }
        var project = getProjectById(projectId);
        project.getApplicants().removeIf(u -> u.getId().equals(userId));
        projectRepository.save(project);
    }

    // Используется в ProjectController для получения проекта с сортированными участниками
    @Transactional(readOnly = true)
    public ProjectResponse getProjectResponse(Long projectId, User caller) {
        var project = getProjectById(projectId);
        var role = getUserRole(projectId, caller.getId());
        var isApplicant = project.getApplicants().contains(caller);
        var members = getSortedMembers(project);
        return ProjectResponse.from(project, role, isApplicant, members);
    }
}
