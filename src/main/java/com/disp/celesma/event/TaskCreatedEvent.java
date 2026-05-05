package com.disp.celesma.event;

import com.disp.celesma.model.Task;
import org.springframework.context.ApplicationEvent;

public class TaskCreatedEvent extends ApplicationEvent {

    private final Task task;

    public TaskCreatedEvent(Object source, Task task) {
        super(source);
        this.task = task;
    }

    public Task getTask() { return task; }
}
