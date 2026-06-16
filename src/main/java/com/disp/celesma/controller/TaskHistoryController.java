package com.disp.celesma.controller;

import com.disp.celesma.dto.task.history.TaskHistoryResponse;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.TaskHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TaskHistoryController {

    private final TaskHistoryService taskHistoryService;

    @PreAuthorize("@projectSecurity.isMemberByTask(#taskId, principal)")
    @GetMapping("/tasks/{taskId}/history")
    public ResponseEntity<List<TaskHistoryResponse>> getTaskHistory(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskHistoryService.getHistoryByTaskId(taskId));
    }
}
