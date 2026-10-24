package org.group12.feature.mealplan.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.group12.common.enums.ResponseCode;

@Getter
@RequiredArgsConstructor
public enum MealPlanResponseCode implements ResponseCode {
    MEAL_PLAN_CREATED("MEALPLAN_201", "Meal plan created successfully"),
    MEAL_PLAN_FETCHED("MEALPLAN_200", "Meal plan fetched successfully"),
    MEAL_PLAN_DELETED("MEALPLAN_200", "Meal plan deleted successfully"),
    MEAL_PLAN_COPIED("MEALPLAN_201", "Meal plan copied successfully"),
    MEAL_PLAN_ENTRY_CREATED("MEALPLAN_201", "Meal plan entry created successfully"),
    MEAL_PLAN_ENTRY_UPDATED("MEALPLAN_200", "Meal plan entry updated successfully"),
    MEAL_PLAN_ENTRY_DELETED("MEALPLAN_200", "Meal plan entry deleted successfully"),
    SHOPPING_LIST_PREVIEW_FETCHED("MEALPLAN_200", "Shopping list preview fetched successfully");

    private final String code;
    private final String message;
}
