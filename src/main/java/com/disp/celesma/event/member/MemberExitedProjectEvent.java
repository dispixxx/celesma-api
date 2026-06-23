package com.disp.celesma.event.member;

import com.disp.celesma.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MemberExitedProjectEvent extends ApplicationEvent {
    private final Long projectId;
    private final Long exitedUserId;
    private final User caller;

    public MemberExitedProjectEvent(Object source, Long projectId, Long exitedUserId, User caller) {
        super(source);
        this.projectId = projectId;
        this.exitedUserId = exitedUserId;
        this.caller = caller;
    }
}
