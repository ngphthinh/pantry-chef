package org.group12.feature.feedback.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.feedback.enums.FeedbackType;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private UUID id;
    private FeedbackType type;
    private String message;
    private String appVersion;
    private String deviceInfo;
    private Instant createdAt;
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String email;
        private String displayName;
        private String avatarUrl;
    }
}