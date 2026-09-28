package com.dogfood.voting.controller;

import com.dogfood.voting.dto.CommentRequest;
import com.dogfood.voting.entity.Comment;
import com.dogfood.voting.service.CommentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/submissions")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{submissionId}/comments")
    public Comment createComment(
            @PathVariable UUID submissionId,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CommentRequest request) {
        return commentService.createComment(submissionId, UUID.fromString(userId), request);
    }

    @GetMapping("/{submissionId}/comments")
    public List<Comment> getComments(@PathVariable UUID submissionId) {
        return commentService.getComments(submissionId);
    }
}
