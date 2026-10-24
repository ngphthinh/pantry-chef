package org.group12.feature.feedback.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.feedback.dto.FeedbackCreateRequest;
import org.group12.feature.feedback.entity.Feedback;
import org.group12.feature.feedback.repository.FeedbackRepository;
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
                .id(appUtil.generateUUID())
                .type(req.getType())
                .message(req.getMessage().trim())
                .appVersion(req.getAppVersion())
                .deviceInfo(req.getDeviceInfo())
                .user(userRepository.getReferenceById(userId))
                .build();

        feedbackRepository.save(feedback);
    }
}