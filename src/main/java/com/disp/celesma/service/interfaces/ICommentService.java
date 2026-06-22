package com.disp.celesma.service.interfaces;

import com.disp.celesma.dto.task.comment.CommentCreateRequest;
import com.disp.celesma.dto.task.comment.CommentResponse;
import com.disp.celesma.model.Comment;
import com.disp.celesma.model.Task;
import com.disp.celesma.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ICommentService {

    @Transactional
    CommentResponse createAndSave(CommentCreateRequest request, String senderName, Long TaskId);

    CommentResponse createAndSave(CommentCreateRequest request, User author, Long TaskId);

    List<CommentResponse> getByTask(Long taskId);

    @Transactional(readOnly = true)
    CommentResponse getCommentById(Long commentId);

    void delete(Long commentId);
}
