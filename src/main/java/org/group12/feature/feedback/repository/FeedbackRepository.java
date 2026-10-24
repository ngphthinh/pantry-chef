package org.group12.feature.feedback.repository;

import org.group12.feature.feedback.entity.Feedback;
import org.group12.feature.feedback.enums.FeedbackType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    @EntityGraph(attributePaths = "user")
    @Query("""
            select f from Feedback f
            where (:type is null or f.type = :type)
              and lower(f.message) like :pattern escape '\\'
            order by f.id desc
            """)
    List<Feedback> findFirstPage(@Param("type") FeedbackType type,
                                 @Param("pattern") String pattern,
                                 Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query("""
            select f from Feedback f
            where f.id < :cursor
              and (:type is null or f.type = :type)
              and lower(f.message) like :pattern escape '\\'
            order by f.id desc
            """)
    List<Feedback> findNextPage(@Param("cursor") UUID cursor,
                                @Param("type") FeedbackType type,
                                @Param("pattern") String pattern,
                                Pageable pageable);
}
