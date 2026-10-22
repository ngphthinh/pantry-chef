package org.group12.feature.mealplan.exception;

import org.group12.common.exception.AppException;

public class MealPlanEntryNotFoundException extends AppException {
    public MealPlanEntryNotFoundException() {
        super(ErrorCodeMealPlan.MEAL_PLAN_ENTRY_NOT_FOUND);
    }
}
