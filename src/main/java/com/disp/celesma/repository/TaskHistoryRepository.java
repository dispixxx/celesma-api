package com.disp.celesma.repository;

import com.disp.celesma.model.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findByTaskIdOrderByChangedAtDesc(Long taskId);

    void deleteAllByTaskId(Long taskId);
}