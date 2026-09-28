package com.dogfood.submission.repository;

import com.dogfood.submission.entity.Submission;
import com.dogfood.submission.entity.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    Optional<Submission> findByEventIdAndTeamId(UUID eventId, UUID teamId);

    Page<Submission> findByEventId(UUID eventId, Pageable pageable);

    @Query(value = """
        SELECT * FROM submissions.submissions s
        WHERE s.event_id = :eventId
        AND s.status = 'SUBMITTED'
        AND (cast(:search as text) IS NULL OR s.search_vector @@ plainto_tsquery('english', cast(:search as text)))
        AND (cast(:trackId as uuid) IS NULL OR s.track_id = cast(:trackId as uuid))
        AND (cast(:tagsStr as text) IS NULL OR s.tech_tags && string_to_array(cast(:tagsStr as text), ','))
        ORDER BY
            CASE WHEN cast(:search as text) IS NOT NULL THEN ts_rank(s.search_vector, plainto_tsquery('english', cast(:search as text))) ELSE 0 END DESC,
            s.submitted_at DESC
        """, nativeQuery = true)
    Page<Submission> searchGallery(
            @Param("eventId") UUID eventId,
            @Param("search") String search,
            @Param("trackId") UUID trackId,
            @Param("tagsStr") String tagsStr,
            Pageable pageable
    );

    List<Submission> findByStatus(SubmissionStatus status);
}
