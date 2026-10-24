package org.group12.feature.mealplan.enums;

import com.fasterxml.jackson.annotation.JsonCreator;


public enum MealType {
    BREAKFAST, LUNCH, DINNER;

    @JsonCreator
    public static MealType from(String value) {
        if (value == null) return null;
        try {
            return valueOf(value.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}