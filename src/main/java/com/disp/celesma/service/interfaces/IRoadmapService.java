package com.disp.celesma.service.interfaces;

import com.disp.celesma.model.RoadmapBranch;
import com.disp.celesma.model.TaskRoadmapEntry;

import java.util.List;

public interface IRoadmapService {
    List<RoadmapBranch> getBranchesByProject(Long projectId);
    List<TaskRoadmapEntry> getTasksInBranch(Long branchId);
    RoadmapBranch createBranch(String name, Long projectId);
    void deleteBranch(Long branchId);
    void renameBranch(Long branchId, String newName);
    void updateBranchOrder(Long projectId, List<Long> branchIds);
    void updateTaskOrderInBranch(Long branchId, List<Long> taskIds);
    void addTaskToBranch(Long taskId, Long branchId, Integer order);
    void removeTaskFromBranch(Long taskId, Long branchId);
    TaskRoadmapEntry getTaskRoadmapEntryByTaskId(Long taskId);
}
