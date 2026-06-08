package com.disp.celesma.controller;

import com.disp.celesma.dto.common.AiDescriptionRequest;
import com.disp.celesma.dto.common.AiTitleRequest;
import com.disp.celesma.dto.task.TaskRequest;
import com.disp.celesma.dto.task.TaskResponse;
import com.disp.celesma.dto.task.TaskStatusRequest;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.IAiService;
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
@RequestMapping("/api/v1")
public class TaskController {

    private final ITaskService taskService;
    private final IAiService aiService;

    // ─────────────────────────────────────────────
    // Задачи через проект
    // ─────────────────────────────────────────────

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse>> getProjectTasks(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTaskAndSave(projectId, principal.getUser(), request));
    }

    // ─────────────────────────────────────────────
    // Прямые операции с задачей по taskId
    // ─────────────────────────────────────────────

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        // projectId не нужен в контроллере — сервис получит его сам при загрузке задачи
        return ResponseEntity.ok(taskService.updateTask(taskId, request, principal.getUser()));
    }

    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<TaskResponse> changeStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                taskService.changeStatus(taskId, request.status(), principal.getUser()));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────
    // AI-генерация
    // ─────────────────────────────────────────────

    /**
     * Генерирует название задачи через AI на основе описания.
     */
    @PostMapping("/tasks/generate-title")
    public ResponseEntity<String> generateTitle(
            @Valid @RequestBody AiTitleRequest request) {
        return ResponseEntity.ok(aiService.generateAiTitle(request.getDescription()));
    }

    /**
     * Универсальная AI-обработка описания задачи.
     * Поддерживает действия: TITLE, IMPROVE, FORMALIZE, SUBTASKS.
     */
    @PostMapping("/tasks/ai-process")
    public ResponseEntity<String> aiProcessDescription(
            @Valid @RequestBody AiDescriptionRequest request) {
        return ResponseEntity.ok(
                aiService.processDescription(request.getDescription(), request.getAction()));
    }
}