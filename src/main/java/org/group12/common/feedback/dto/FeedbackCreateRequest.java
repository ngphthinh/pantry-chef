package org.group12.common.feedback.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.group12.common.feedback.enums.FeedbackType;
public record FeedbackCreateRequest(
        @NotNull FeedbackType type,
        @NotBlank @Size(max = 2000) String message,
        @Size(max = 50) String appVersion,
        @Size(max = 255) String deviceInfo
) {
}
