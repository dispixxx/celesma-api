package com.disp.celesma.event;

import com.disp.celesma.model.Task;
import com.disp.celesma.model.enums.TaskStatus;
import org.springframework.context.ApplicationEvent;

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

    public Task getTask() { return task; }
    public TaskStatus getOldStatus() { return oldStatus; }
    public TaskStatus getNewStatus() { return newStatus; }
}
