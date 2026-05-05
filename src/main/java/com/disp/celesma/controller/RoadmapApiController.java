package com.disp.celesma.controller;

import com.disp.celesma.model.RoadmapBranch;
import com.disp.celesma.model.TaskRoadmapEntry;
import com.disp.celesma.service.interfaces.IRoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/roadmap")
@RequiredArgsConstructor
public class RoadmapApiController {

    private final IRoadmapService roadmapService;

    @GetMapping("/branches")
    public ResponseEntity<List<RoadmapBranch>> getBranches(@PathVariable Long projectId) {
        return ResponseEntity.ok(roadmapService.getBranchesByProject(projectId));
    }

    @PostMapping("/branches")
    public ResponseEntity<RoadmapBranch> createBranch(
            @PathVariable Long projectId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roadmapService.createBranch(body.get("name"), projectId));
    }

    @DeleteMapping("/branches/{branchId}")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long branchId) {
        roadmapService.deleteBranch(branchId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/branches/{branchId}/tasks")
    public ResponseEntity<List<TaskRoadmapEntry>> getTasksInBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(roadmapService.getTasksInBranch(branchId));
    }

    @PostMapping("/branches/{branchId}/tasks")
    public ResponseEntity<Void> addTask(
            @PathVariable Long branchId,
            @RequestBody Map<String, Long> body) {
        roadmapService.addTaskToBranch(body.get("taskId"), branchId, null);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/branches/{branchId}/tasks/{taskId}")
    public ResponseEntity<Void> removeTask(
            @PathVariable Long branchId,
            @PathVariable Long taskId) {
        roadmapService.removeTaskFromBranch(taskId, branchId);
        return ResponseEntity.noContent().build();
    }
}
