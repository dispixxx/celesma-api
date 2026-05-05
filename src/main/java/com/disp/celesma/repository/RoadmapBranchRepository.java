package com.disp.celesma.repository;

import com.disp.celesma.model.RoadmapBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapBranchRepository extends JpaRepository<RoadmapBranch, Long> {
    List<RoadmapBranch> findByProjectIdOrderBySortOrderAsc(Long projectId);
    List<RoadmapBranch> findByProjectIdAndIdIn(Long projectId, List<Long> branchIds);
    Integer countByProjectId(Long projectId);
    RoadmapBranch getRoadmapBranchById(Long branchId);
}
