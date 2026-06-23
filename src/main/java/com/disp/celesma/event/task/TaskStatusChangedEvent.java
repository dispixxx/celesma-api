package com.disp.celesma.event.task;

import com.disp.celesma.model.Task;
import com.disp.celesma.model.enums.TaskStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TaskStatusChangedEvent extends ApplicationEvent {

    private final Task task;
    private final TaskStatus oldStatus;
    private final TaskStatus newStatus;

    public TaskStatusChangedEvent(Object source, Task task, TaskStatus oldStatus, TaskStatus newStatus) {
        super(source);
        this.task = task;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

}
