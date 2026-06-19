package com.disp.celesma.service;

import com.disp.celesma.dto.task.comment.CommentResponse;
import com.disp.celesma.mapper.CommentMapper;
import com.disp.celesma.model.Comment;
import com.disp.celesma.model.User;
import com.disp.celesma.repository.CommentRepository;
import com.disp.celesma.service.interfaces.ICommentService;
import com.disp.celesma.service.interfaces.ITaskService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {

    private final CommentRepository commentRepository;

    private final ITaskService taskService;

    private final CommentMapper commentMapper;

    private Comment getCommentEntityById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment %d not found".formatted(commentId)));
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {
        var comment = getCommentEntityById(commentId);
        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse createAndSave(String text, User author, Long taskId) {

        var task = taskService.getTaskEntityById(taskId);

        var comment = Comment.builder()
                .text(text)
                .author(author)
                .task(task)
                .createdAt(LocalDateTime.now())
                .build();
        var saved = commentRepository.save(comment);
        return commentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getByTask(Long taskId) {
        var comments = commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
        return comments.stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new EntityNotFoundException("Comment %d not found".formatted(commentId));
        }
        commentRepository.deleteById(commentId);
    }
}
