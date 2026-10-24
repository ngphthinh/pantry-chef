package org.group12.feature.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.feedback.enums.FeedbackType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FeedbackCreateRequest {
    @NotNull
    private FeedbackType type;
    @NotBlank
    @Size(max = 2000)
    private String message;
    @Size(max = 50)
    private String appVersion;
    @Size(max = 255)
    private String deviceInfo;
}
