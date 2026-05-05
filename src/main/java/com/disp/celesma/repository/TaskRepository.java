package com.disp.celesma.repository;

import com.disp.celesma.model.Task;
import com.disp.celesma.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByProjectIdAndAssigneeId(Long projectId, Long assigneeId);
    List<Task> findByProjectIdAndCreatorId(Long projectId, Long creatorId);

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> countByProjectIdGroupByStatus(@Param("projectId") Long projectId);

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.assignee.id = :userId GROUP BY t.status")
    List<Object[]> countByProjectIdAndUserIdGroupByStatus(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
