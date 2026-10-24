package org.group12.feature.mealplan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanResponse {
    private UUID id;
    private LocalDate weekStart;
    @Builder.Default
    private List<MealPlanEntryResponse> entries = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
}
