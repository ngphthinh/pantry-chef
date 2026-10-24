package org.group12.feature.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.mealplan.enums.MealType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// MealPlanResponse
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
        private Instant date;
        private MealType mealType;
        private UUID recipeId;        // luôn có giá trị
        private Integer servings;
        private String recipeTitle;   // chỉ để hiển thị, request lưu bỏ qua field này
    }
}