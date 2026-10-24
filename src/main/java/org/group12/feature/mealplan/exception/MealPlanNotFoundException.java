package org.group12.feature.mealplan.exception;

import org.group12.common.exception.AppException;

public class MealPlanNotFoundException extends AppException {
    public MealPlanNotFoundException() {
        super(ErrorCodeMealPlan.MEAL_PLAN_NOT_FOUND);
    }
}
