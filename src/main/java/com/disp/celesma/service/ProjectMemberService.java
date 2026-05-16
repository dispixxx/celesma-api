package com.disp.celesma.service;

import com.disp.celesma.model.ProjectMember;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.ProjectRole;
import com.disp.celesma.repository.ProjectMemberRepository;
import com.disp.celesma.service.interfaces.IProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectMemberService implements IProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;

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
        return role == ProjectRole.ADMIN || role == ProjectRole.MODERATOR;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProjectIdAndUserId(Long projectId, Long userId) {
        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectMember getProjectMemberByProjectIdAndUserId(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Project member not found for project ID: " + projectId + " and user ID: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectMember getProjectMemberById(Long memberId) {
        return projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Project member not found with ID: " + memberId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMember> getAllByUser(User user) {
        return projectMemberRepository.findAllByUser(user);
    }

    @Override
    @Transactional
    public ProjectMember save(ProjectMember member) {
        return projectMemberRepository.save(member);
    }
}
