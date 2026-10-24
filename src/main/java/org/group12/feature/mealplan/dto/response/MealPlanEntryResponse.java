package org.group12.feature.mealplan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.mealplan.enums.MealType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanEntryResponse {
    private UUID id;
    private LocalDate date;
    private MealType mealType;
    private UUID recipeId;
    private String recipeTitle;
    private String recipeImageUrl;
    private Integer servings;
    private boolean cooked;
    private Instant createdAt;
    private Instant updatedAt;
}