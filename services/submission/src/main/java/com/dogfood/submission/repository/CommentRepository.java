package com.dogfood.submission.repository;

import com.dogfood.submission.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findBySubmissionIdOrderByCreatedAtDesc(UUID submissionId);
}
