package com.disp.celesma.controller;

import com.disp.celesma.dto.task.TaskRequest;
import com.disp.celesma.dto.task.TaskResponse;
import com.disp.celesma.dto.task.TaskStatusRequest;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.ITaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final ITaskService taskService;

    @GetMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse>> getProjectTasks(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @PostMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(projectId, principal.getUser(), request));
    }

    @GetMapping("/api/v1/tasks/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @PutMapping("/api/v1/tasks/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request, principal.getUser()));
    }

    @PatchMapping("/api/v1/tasks/{taskId}/status")
    public ResponseEntity<TaskResponse> changeStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.changeStatus(taskId, request.status(), principal.getUser()));
    }

    @DeleteMapping("/api/v1/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
