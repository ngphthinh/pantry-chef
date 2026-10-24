package org.group12.feature.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.mealplan.enums.MealType;

import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMealPlanDraft {
    private List<Item> items;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Integer dayIndex;
        private MealType mealType;
        private String recipeId;      // String để id sai không làm Jackson ném lỗi
    }
}