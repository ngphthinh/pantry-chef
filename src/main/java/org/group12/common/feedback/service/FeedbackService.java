package org.group12.common.feedback.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.common.feedback.dto.FeedbackCreateRequest;
import org.group12.common.feedback.entity.Feedback;
import org.group12.common.feedback.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final AppUtil appUtil;

    @Transactional
    public void create(FeedbackCreateRequest req) {
        UUID userId = appUtil.userIdFromAuthentication();

        Feedback feedback = Feedback.builder()
                .id(UUID.randomUUID())
                .type(req.type())
                .message(req.message().trim())
                .appVersion(req.appVersion())
                .deviceInfo(req.deviceInfo())
                .user(userRepository.getReferenceById(userId))
                .build();

        feedbackRepository.save(feedback);
    }
}