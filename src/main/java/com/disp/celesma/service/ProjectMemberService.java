package com.disp.celesma.service;

import com.disp.celesma.dto.member.MemberResponse;
import com.disp.celesma.event.member.MemberExitedProjectEvent;
import com.disp.celesma.mapper.MemberMapper;
import com.disp.celesma.model.Project;
import com.disp.celesma.model.ProjectMember;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.ProjectRole;
import com.disp.celesma.repository.ProjectMemberRepository;
import com.disp.celesma.repository.ProjectRepository;
import com.disp.celesma.service.interfaces.IProjectMemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjectMemberService implements IProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final MemberMapper memberMapper;
    private final ApplicationEventPublisher eventPublisher;

    Map<ProjectRole, Integer> ROLE_ORDER = Map.of(
            ProjectRole.OWNER, 1,
            ProjectRole.ADMIN, 2,
            ProjectRole.MODERATOR, 3,
            ProjectRole.MEMBER, 4,
            ProjectRole.VIEWER, 5
    );


    private Project getProjectEntityOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project %d not found".formatted(projectId)));
    }

    public ProjectMember getProjectMemberEntityById(Long memberId) {
        return projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Project member not found with ID: " + memberId));
    }

    @Override
    @Transactional
    public ProjectMember createAndAddMemberToProject(Project project, User user, ProjectRole role) {

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(user)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();

        return projectMemberRepository.save(member);
    }

    /**
     * Validates that the specified user is a member of the given project.<br>
     * Throws {@link AccessDeniedException} if the user is not a project member.<br>
     * Throws {@link EntityNotFoundException} if the project not found.
     *
     * @param projectId the ID of the project to check membership against
     * @param userId    the ID of the user whose membership is being validated
     */
    @Transactional(readOnly = true)
    public boolean validateIsMember(Long projectId, Long userId) {
        getProjectEntityOrThrow(projectId); //validate project
        if (!isMember(projectId, userId)) {
            throw new AccessDeniedException(
                    "Пользователь %d не является участником проекта %d"
                            .formatted(userId, projectId));
        }
        return true;
    }

    public boolean isMember(Long projectId, Long userId) {
        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }


    //TODO /* APPLICANT SERVICE */
    @Transactional
    public MemberResponse acceptApplicant(Long projectId, Long userId, User caller) {
        if (!isPrivileged(projectId, caller.getId())) {
            throw new AccessDeniedException("Нет прав для принятия заявок");
        }

        var project = getProjectEntityOrThrow(projectId);

        var applicant = project.getApplicants().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена"));

        project.getApplicants().remove(applicant);
        var member = createAndAddMemberToProject(project, applicant, ProjectRole.MEMBER);


        return memberMapper.toResponse(member);
    }

    //TODO /* APPLICANT SERVICE */
    @Transactional
    public void declineApplicant(Long projectId, Long userId, User caller) {
        if (!isPrivileged(projectId, caller.getId())) {
            throw new AccessDeniedException("Нет прав для отклонения заявок");
        }

        var project = getProjectEntityOrThrow(projectId);

        project.getApplicants().removeIf(u -> u.getId().equals(userId));
        projectRepository.save(project);
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
    public boolean isPrivileged(Long projectId, Long userId) {
        var role = getUserRole(projectId, userId);
        return role == ProjectRole.OWNER || role == ProjectRole.ADMIN || role == ProjectRole.MODERATOR;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectMember getProjectMemberByProjectIdAndUserId(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Project member not found for project ID: " + projectId + " and user ID: " + userId));
    }


    @Override
    @Transactional(readOnly = true)
    public MemberResponse getProjectMemberById(Long memberId) {
        return memberMapper.toResponse(getProjectMemberEntityById(memberId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMember> getAllByUserWithProjectAndOwner(User user) {
        return projectMemberRepository.findAllByUserWithProjectAndOwner(user);
    }

    @Override
    @Transactional
    public ProjectMember save(ProjectMember member) {
        return projectMemberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> getSortedMembersByProjectId(Long projectId) {

        var project = getProjectEntityOrThrow(projectId);

        return project.getMembers().stream()
                .sorted(Comparator
                        .<ProjectMember, Integer>comparing(m -> ROLE_ORDER.getOrDefault(m.getRole(), 99))
                        .thenComparing(m -> m.getJoinedAt() != null ? m.getJoinedAt() : LocalDateTime.MIN))
                .map(memberMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public MemberResponse updateMemberRole(User caller, Long projectId, Long memberId, ProjectRole newRole) {
        if (!isPrivileged(projectId, caller.getId())) {
            throw new IllegalStateException("Нет прав для изменения ролей");
        }

        ProjectMember member = getProjectMemberEntityById(memberId);


        if (!member.getProject().getId().equals(projectId)) {
            throw new IllegalStateException("Участник не принадлежит этому проекту");
        }

        if (member.getRole() == ProjectRole.OWNER) {
            throw new IllegalStateException("Нельзя изменить роль владельца проекта");
        }

        if (getUserRole(projectId, caller.getId()) == ProjectRole.MODERATOR
                && newRole == ProjectRole.ADMIN) {
            throw new IllegalStateException("Модератор не может назначать администраторов");
        }

        member.setRole(newRole);
        return memberMapper.toResponse(member);
    }

    @Transactional
    @Override
    public void removeMember(Long projectId, Long memberId, User caller) {
        if (!isPrivileged(projectId, caller.getId())) {
            throw new IllegalStateException("Нет прав для удаления участников");
        }

        var member = getProjectMemberEntityById(memberId);
        var project = member.getProject();

        if (!member.getProject().getId().equals(projectId)) {
            throw new IllegalStateException("Участник не принадлежит этому проекту");
        }

        if (member.getRole() == ProjectRole.OWNER) {
            throw new IllegalStateException("Нельзя удалить владельца проекта");
        }

        if (ROLE_ORDER.get(member.getRole()) <= ROLE_ORDER.get(getUserRole(projectId, caller.getId()))) {
            throw new IllegalStateException("Нельзя удалить: Роль сильнее или такая же");
        }

        project.getMembers().remove(member);
        projectRepository.save(project);

        eventPublisher.publishEvent(new MemberExitedProjectEvent(this, projectId, member.getUser().getId(), caller));

        //  taskService.reassignAndHoldTasks(projectId, member.getUser().getId(), caller);
    }

    @Transactional
    @Override
    public void exitFromProject(Long projectId, User caller, Long memberId) {

        var member = getProjectMemberEntityById(memberId);
        var project = member.getProject();

        if (member.getRole() == ProjectRole.OWNER) {
            throw new IllegalStateException("Нельзя выйти. ВЫ ВЛАДЕЛЕЦ ПРОЕКТА");
        }

        project.getMembers().remove(member);
        projectRepository.save(project);

        eventPublisher.publishEvent(new MemberExitedProjectEvent(this, projectId, member.getUser().getId(), caller));

        //  taskService.reassignAndHoldTasks(projectId, member.getUser().getId(), caller);
    }
}
