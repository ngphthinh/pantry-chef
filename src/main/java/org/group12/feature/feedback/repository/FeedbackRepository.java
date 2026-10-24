package org.group12.feature.feedback.repository;

import org.group12.feature.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
}
