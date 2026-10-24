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
    AI_SUBSTITUTIONS_SUCCESS("AI_202", "Get substitutions successfully"),
    AI_MEAL_PLAN_SUCCESS("AI_203", "Generate meal plan successfully"),
    AI_RECIPE_IDEAS_SUCCESS("AI_204", "Get recipe ideas successfully"),
    ADAPT_RECIPE_SUCCESS("AI_205", "Adapt recipe successfully"),
    ;
    private final String code;
    private final String message;
}
