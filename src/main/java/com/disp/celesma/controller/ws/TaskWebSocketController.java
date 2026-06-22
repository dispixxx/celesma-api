package com.disp.celesma.controller.ws;

import com.disp.celesma.dto.task.TaskResponse;
import com.disp.celesma.dto.task.TaskStatusUpdateRequest;
import com.disp.celesma.service.interfaces.ITaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class TaskWebSocketController {

    private final ITaskService taskService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/task/{taskId}/status")
    public void handleStatusChange(@DestinationVariable Long taskId,
                                   @Payload TaskStatusUpdateRequest request,
                                   Principal principal) {

        String username = principal.getName();
        TaskResponse updated = taskService.changeStatus(taskId, request.status(), username);

        // Пушим всем в проекте
        messagingTemplate.convertAndSend(
                "/topic/project/" + updated.projectId() + "/tasks",
                updated
        );
    }
}
