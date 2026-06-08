package com.disp.celesma.service;

import com.disp.celesma.model.RoadmapBranch;
import com.disp.celesma.model.TaskRoadmapEntry;
import com.disp.celesma.repository.RoadmapBranchRepository;
import com.disp.celesma.repository.TaskRoadmapEntryRepository;
import com.disp.celesma.service.interfaces.IProjectService;
import com.disp.celesma.service.interfaces.IRoadmapService;
import com.disp.celesma.service.interfaces.ITaskService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadmapService implements IRoadmapService {

    private final RoadmapBranchRepository branchRepo;
    private final TaskRoadmapEntryRepository entryRepo;
    private final IProjectService projectService;
    private final ITaskService taskService;

    @Override
    public List<RoadmapBranch> getBranchesByProject(Long projectId) {
        return branchRepo.findByProjectIdOrderBySortOrderAsc(projectId);
    }

    @Override
    public List<TaskRoadmapEntry> getTasksInBranch(Long branchId) {
        return entryRepo.findTasksByBranchIdOrderByOrderInBranch(branchId);
    }

    @Override
    public RoadmapBranch createBranch(String name, Long projectId) {
        var project = projectService.getProjectEntityById(projectId);
        var branch = RoadmapBranch.builder()
                .name(name)
                .project(project)
                .sortOrder(branchRepo.countByProjectId(projectId) + 1)
                .build();
        return branchRepo.save(branch);
    }

    @Override
    @Transactional
    public void deleteBranch(Long branchId) {
        branchRepo.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Ветка не найдена"));
        branchRepo.deleteById(branchId);
    }

    @Override
    @Transactional
    public void renameBranch(Long branchId, String newName) {
        var branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Ветка не найдена"));
        if (newName.length() < 2 || newName.length() > 50) {
            throw new IllegalArgumentException("Название должно быть от 2 до 50 символов");
        }
        branch.setName(newName);
        branchRepo.save(branch);
    }

    @Override
    public void updateBranchOrder(Long projectId, List<Long> branchIds) {
        var branches = branchRepo.findByProjectIdAndIdIn(projectId, branchIds);
        Map<Long, RoadmapBranch> map = branches.stream()
                .collect(Collectors.toMap(RoadmapBranch::getId, b -> b));
        for (int i = 0; i < branchIds.size(); i++) {
            var branch = map.get(branchIds.get(i));
            if (branch != null) branch.setSortOrder(i + 1);
        }
        branchRepo.saveAll(branches);
    }

    @Override
    public void updateTaskOrderInBranch(Long branchId, List<Long> taskIds) {
        var entries = entryRepo.findByBranchId(branchId);
        Map<Long, TaskRoadmapEntry> map = entries.stream()
                .collect(Collectors.toMap(e -> e.getTask().getId(), e -> e));
        List<TaskRoadmapEntry> toSave = new ArrayList<>();
        for (int i = 0; i < taskIds.size(); i++) {
            var entry = map.get(taskIds.get(i));
            if (entry != null) {
                entry.setOrderInBranch(i + 1);
                toSave.add(entry);
            }
        }
        entryRepo.saveAll(toSave);
    }

    @Override
    @Transactional
    public void addTaskToBranch(Long taskId, Long branchId, Integer order) {
        var task = taskService.getTaskEntityById(taskId);
        var branch = branchRepo.getRoadmapBranchById(branchId);

        Optional<TaskRoadmapEntry> existing = entryRepo.findByTaskIdAndBranchId(taskId, branchId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Задача уже в этой ветке");
        }

        if (branch.getEntries().isEmpty()) order = 1;

        var entry = TaskRoadmapEntry.builder()
                .task(task)
                .branch(branch)
                .orderInBranch(order != null ? order : getNextOrder(branchId))
                .build();
        entryRepo.save(entry);
    }

    @Override
    @Transactional
    public void removeTaskFromBranch(Long taskId, Long branchId) {
        entryRepo.deleteByTaskIdAndBranchId(taskId, branchId);
    }

    @Override
    public TaskRoadmapEntry getTaskRoadmapEntryByTaskId(Long taskId) {
        return entryRepo.findByTaskId(taskId).orElse(null);
    }

    private Integer getNextOrder(Long branchId) {
        Integer max = entryRepo.findMaxOrderInBranch(branchId);
        return max != null ? max + 1 : 1;
    }
}
