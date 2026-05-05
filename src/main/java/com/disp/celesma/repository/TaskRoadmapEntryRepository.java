package com.disp.celesma.repository;

import com.disp.celesma.model.TaskRoadmapEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskRoadmapEntryRepository extends JpaRepository<TaskRoadmapEntry, Long> {
    List<TaskRoadmapEntry> findTasksByBranchIdOrderByOrderInBranch(Long branchId);
    List<TaskRoadmapEntry> findByBranchId(Long branchId);
    Optional<TaskRoadmapEntry> findByTaskIdAndBranchId(Long taskId, Long branchId);

    @Query("SELECT MAX(e.orderInBranch) FROM TaskRoadmapEntry e WHERE e.branch.id = :branchId")
    Integer findMaxOrderInBranch(Long branchId);

    void deleteByTaskIdAndBranchId(Long taskId, Long branchId);
    List<TaskRoadmapEntry> findByBranchIdOrderByOrderInBranchAsc(Long branchId);
    Optional<TaskRoadmapEntry> findByTaskId(Long taskId);
}
