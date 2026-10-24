package org.group12.feature.feedback.service;

import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.CursorResponse;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.feedback.dto.request.FeedbackCreateRequest;
import org.group12.feature.feedback.dto.response.FeedbackResponse;
import org.group12.feature.feedback.entity.Feedback;
import org.group12.feature.feedback.enums.FeedbackType;
import org.group12.feature.feedback.mapper.FeedbackMapper;
import org.group12.feature.feedback.repository.FeedbackRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final AppUtil appUtil;
    private final FeedbackMapper feedbackMapper;

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

    @Transactional(readOnly = true)
    public CursorResponse<FeedbackResponse> getAllFeedback(
            UUID cursor, int size, String keyword, FeedbackType type) {
        Pageable pageable = PageRequest.of(0, size + 1);
        String pattern = toPattern(keyword);

        List<Feedback> items = cursor == null
                ? feedbackRepository.findFirstPage(type, pattern, pageable)
                : feedbackRepository.findNextPage(cursor, type, pattern, pageable);

        return appUtil.buildCursorResponse(items, size, Feedback::getId, feedbackMapper::toFeedbackResponse);
    }

    // escape % _ \ để keyword không bị hiểu là wildcard
    private String toPattern(String keyword) {
        if (keyword == null || keyword.isBlank()) return "%";
        String escaped = keyword.trim().toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}