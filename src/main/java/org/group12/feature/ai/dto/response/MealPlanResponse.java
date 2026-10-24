package org.group12.feature.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanResponse {

    private Instant weekStart;
    private List<Entry> entries;

    public static MealPlanResponse empty(Instant weekStart) {
        return MealPlanResponse.builder().weekStart(weekStart).entries(List.of()).build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entry {
        private Instant date;          // weekStart + dayIndex
        private String mealType;         // BREAKFAST | LUNCH | DINNER
        private String recipeTitle;
        private UUID recipeId;           // null nếu không trùng tên recipe nào trong DB
        private Integer readyInMinutes;
    }
}