package com.dogfood.voting.repository;

import com.dogfood.voting.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findBySubmissionId(UUID submissionId);
}
