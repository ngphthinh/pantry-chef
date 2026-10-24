package org.group12.feature.mealplan.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanCreateRequest {
    @NotNull(message = "Week start date is required")
    private LocalDate weekStart;

    private List<@Valid MealPlanEntryCreateRequest> entries;
}
