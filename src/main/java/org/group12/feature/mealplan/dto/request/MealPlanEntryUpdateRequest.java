package org.group12.feature.mealplan.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.mealplan.enums.MealType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanEntryUpdateRequest {
    private LocalDate date;
    private MealType mealType;

    @Positive(message = "Servings must be greater than 0")
    private Integer servings;

    private Boolean cooked;
}
