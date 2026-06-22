package com.disp.celesma.controller.ws;

import com.disp.celesma.dto.task.comment.CommentCreateRequest;
import com.disp.celesma.dto.task.comment.CommentResponse;
import com.disp.celesma.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CommentWebSocketController {

    private final CommentService commentService;
    private final SimpMessagingTemplate messagingTemplate;

    // Клиент шлёт на /app/task/{taskId}/comment
    @MessageMapping("/task/{taskId}/comment")
//    @PreAuthorize("@projectSecurity.isMemberByTask(#taskId, principal)")
    public void handleComment(@DestinationVariable Long taskId,
                              @Payload CommentCreateRequest request,
                              Principal principal) {

        CommentResponse saved = commentService.createAndSave(request, principal.getName(), taskId);

        // Сервер пушит всем подписчикам /topic/task/{taskId}/comments
        messagingTemplate.convertAndSend(
                "/topic/task/" + taskId + "/comments",
                saved
        );
    }
}
