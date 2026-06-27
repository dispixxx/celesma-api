package com.disp.celesma.repository;

import com.disp.celesma.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    @Query("""
        SELECT a FROM TaskAttachment a
        JOIN FETCH a.uploadedBy
        WHERE a.task.id = :taskId
        ORDER BY a.createdAt DESC
    """)
    List<TaskAttachment> findByTaskIdWithDetails(@Param("taskId") Long taskId);
}
