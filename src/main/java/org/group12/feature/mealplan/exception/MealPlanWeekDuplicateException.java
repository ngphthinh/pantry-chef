package org.group12.feature.mealplan.exception;

import org.group12.common.exception.AppException;

public class MealPlanWeekDuplicateException extends AppException {
    public MealPlanWeekDuplicateException() {
        super(ErrorCodeMealPlan.MEAL_PLAN_WEEK_DUPLICATE);
    }
}
