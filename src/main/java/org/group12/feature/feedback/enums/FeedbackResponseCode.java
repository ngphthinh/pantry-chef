package org.group12.feature.feedback.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.group12.common.enums.ResponseCode;

@RequiredArgsConstructor
@Getter
public enum FeedbackResponseCode implements ResponseCode {
    GET_FEEDBACKS_SUCCESS("FEEDBACK_200", "Get feedbacks successfully");
    private final String code;
    private final String message;
}
