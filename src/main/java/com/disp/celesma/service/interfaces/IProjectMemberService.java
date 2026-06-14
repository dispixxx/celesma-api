package com.disp.celesma.service.interfaces;

import com.disp.celesma.dto.member.MemberResponseDto;
import com.disp.celesma.model.Project;
import com.disp.celesma.model.ProjectMember;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.ProjectRole;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IProjectMemberService {

    boolean isMember(Long projectId, Long id);

    void validateIsMember(Long projectId, Long id);

    ProjectRole getUserRole(Long projectId, Long userId);

    boolean isPrivileged(Long projectId, Long userId);

    ProjectMember getProjectMemberByProjectIdAndUserId(Long projectId, Long userId);

    ProjectMember getProjectMemberEntityById(Long memberId);

    MemberResponseDto getProjectMemberById(Long memberId);

    List<ProjectMember> getAllByUserWithProjectAndOwner(User user);

    ProjectMember save(ProjectMember member);

    List<MemberResponseDto> getSortedMembersByProjectId(Long projectId);

    ProjectMember createAndAddMemberToProject(Project project, User user, ProjectRole projectRole);


    MemberResponseDto updateMemberRole(User caller, Long projectId, Long memberId, ProjectRole newRole);

    void removeMember(Long projectId, Long memberId, User user);

    void exitFromProject(Long projectId, User user, Long memberId);
}
