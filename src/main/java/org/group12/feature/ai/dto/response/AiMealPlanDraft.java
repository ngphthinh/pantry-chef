package org.group12.feature.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.mealplan.enums.MealType;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMealPlanDraft {
    private List<Item> items;


    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Item {
        private Integer dayIndex;            // 0..days-1
        private MealType mealType;             // BREAKFAST | LUNCH | DINNER
        private String recipeTitle;
        private Integer readyInMinutes;
        private List<String> usedIngredients;
    }
}