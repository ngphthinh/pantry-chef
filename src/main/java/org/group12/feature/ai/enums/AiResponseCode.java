package org.group12.feature.ai.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.group12.common.enums.ResponseCode;

@RequiredArgsConstructor
@Getter
public enum AiResponseCode implements ResponseCode {
    AI_RESPONSE_SUCCESS("AI_200", "AI response successfully"),
    AI_RESPONSE_ERROR("AI_500", "AI response error"),
    AI_RECOGNIZE_INGREDIENT_SUCCESS("AI_201", "Recognize ingredient successfully"),

    ;
    private final String code;
    private final String message;
}
