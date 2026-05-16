package com.disp.celesma.service.interfaces;

import com.disp.celesma.model.ProjectMember;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.ProjectRole;

import java.util.List;
import java.util.Optional;

public interface IProjectMemberService {
    ProjectRole getUserRole(Long projectId, Long userId);
    boolean isPrivileged(Long projectId, Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    ProjectMember getProjectMemberByProjectIdAndUserId(Long projectId, Long userId);
    ProjectMember getProjectMemberById(Long memberId);
    List<ProjectMember> getAllByUser(User user);
    ProjectMember save(ProjectMember member);
}
