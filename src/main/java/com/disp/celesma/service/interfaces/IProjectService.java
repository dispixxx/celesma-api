package com.disp.celesma.service.interfaces;

import com.disp.celesma.dto.applicant.ApplicantResponseDto;
import com.disp.celesma.dto.project.ProjectCreateRequest;
import com.disp.celesma.dto.project.ProjectResponseDto;
import com.disp.celesma.dto.project.ProjectUpdateRequest;
import com.disp.celesma.model.Project;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.ProjectRole;
import jakarta.validation.Valid;

import java.util.List;

public interface IProjectService {
    ProjectResponseDto createProjectAndSave(User user, @Valid ProjectCreateRequest request);
    Project getProjectEntityById(Long id);
    ProjectResponseDto getProjectById(Long id);
    List<ProjectResponseDto> getUserProjects(User user);
    ProjectRole getUserRole(Long projectId, Long userId);
    List<ProjectResponseDto> searchProjects(User user, String query);
    void exitFromProject(Long projectId, User user);
    ApplicantResponseDto addJoinRequest(Long projectId, User user);
    void cancelJoinRequest(Long projectId, User user);
    boolean isApplicant(Long projectId, User user);
    ProjectResponseDto updateProjectAndSave(Long projectId, User caller, ProjectUpdateRequest request);
    void deleteProject(Long projectId, User caller);
    List<ApplicantResponseDto> getApplicants(Long projectId);
/*    void acceptApplicant(Long projectId, Long userId, User caller);
    void declineApplicant(Long projectId, Long userId, User caller);*/
//    ProjectResponse getProjectResponse(Long projectId, User user);
/*    void updateMemberRole(Long projectId, Long memberId, ProjectRole newRole, User caller);
    void removeMember(Long projectId, Long memberId, User caller);*/

}
