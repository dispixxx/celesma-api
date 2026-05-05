package com.disp.celesma.service.interfaces;

import com.disp.celesma.dto.project.ProjectRequest;
import com.disp.celesma.dto.project.ProjectResponse;
import com.disp.celesma.model.Project;
import com.disp.celesma.model.User;
import com.disp.celesma.model.enums.ProjectRole;

import java.util.List;

public interface IProjectService {
    Project createProject(User owner, ProjectRequest request);
    Project getProjectById(Long id);
    List<ProjectResponse> getUserProjects(User user);
    ProjectRole getUserRole(Long projectId, Long userId);
    boolean isUserMember(Long projectId, Long userId);
    List<ProjectResponse> searchProjects(User user, String query);
    void exitFromProject(Long projectId, User user);
    void addJoinRequest(Long projectId, User user);
    void cancelJoinRequest(Long projectId, User user);
    boolean isApplicant(Long projectId, User user);
    Project updateProject(Long projectId, User caller, ProjectRequest request);
    void deleteProject(Long projectId, User caller);
    List<User> getApplicants(Long projectId);
    void acceptApplicant(Long projectId, Long userId, User caller);
    void declineApplicant(Long projectId, Long userId, User caller);
    ProjectResponse getProjectResponse(Long projectId, User user);
}
