package com.dogfood.voting.service;

import com.dogfood.voting.dto.CommentRequest;
import com.dogfood.voting.entity.Comment;
import com.dogfood.voting.repository.CommentRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final RabbitTemplate rabbitTemplate;

    public CommentService(CommentRepository commentRepository, RabbitTemplate rabbitTemplate) {
        this.commentRepository = commentRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Comment createComment(UUID submissionId, UUID userId, CommentRequest request) {
        Comment comment = new Comment();
        comment.setEventId(request.eventId());
        comment.setSubmissionId(submissionId);
        comment.setUserId(userId);
        comment.setContent(request.content());
        Comment saved = commentRepository.save(comment);
        
        rabbitTemplate.convertAndSend("dogfood.audit", "", Map.of("event", "comment.created", "commentId", saved.getId()));
        return saved;
    }

    public List<Comment> getComments(UUID submissionId) {
        return commentRepository.findBySubmissionId(submissionId);
    }
}
