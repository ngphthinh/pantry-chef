package org.group12.feature.mealplan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.mealplan.enums.MealType;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanEntryCreateRequest {
    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    @Schema(description = "Optional recipe ID")
    private UUID recipeId;

    @Positive(message = "Servings must be greater than 0")
    private Integer servings;
}
