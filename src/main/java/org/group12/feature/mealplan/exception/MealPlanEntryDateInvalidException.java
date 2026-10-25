package org.group12.feature.mealplan.exception;

import org.group12.common.exception.AppException;

public class MealPlanEntryDateInvalidException extends AppException {
    public MealPlanEntryDateInvalidException() {
        super(ErrorCodeMealPlan.MEAL_PLAN_ENTRY_DATE_INVALID);
    }
}
