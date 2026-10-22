package org.group12.feature.mealplan.exception;

import lombok.Getter;
import org.group12.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodeMealPlan implements BaseErrorCode {
    MEAL_PLAN_NOT_FOUND("MEALPLAN_404", "Meal plan not found", HttpStatus.NOT_FOUND),
    MEAL_PLAN_ENTRY_NOT_FOUND("MEALPLAN_ENTRY_404", "Meal plan entry not found", HttpStatus.NOT_FOUND),
    MEAL_PLAN_WEEK_DUPLICATE("MEALPLAN_409", "Meal plan for this week already exists", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeMealPlan(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
