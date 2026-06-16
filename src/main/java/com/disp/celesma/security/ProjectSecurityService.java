package com.disp.celesma.security;

import com.disp.celesma.service.interfaces.IProjectMemberService;
import com.disp.celesma.service.interfaces.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("projectSecurity")
@RequiredArgsConstructor
public class ProjectSecurityService {

    private final IProjectMemberService projectMemberService;
    private final ITaskService taskService;

    public boolean isMember(Long projectId, UserPrincipal principal) {
        return projectMemberService.validateIsMember(projectId, principal.getUser().getId());
    }

    public boolean isMemberByTask(Long taskId, UserPrincipal principal) {
        var task = taskService.getTaskEntityById(taskId);
        return projectMemberService.validateIsMember(task.getProject().getId(), principal.getUser().getId());

    }
}