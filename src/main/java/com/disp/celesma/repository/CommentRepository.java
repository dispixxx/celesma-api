package com.disp.celesma.repository;

import com.disp.celesma.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    List<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}
