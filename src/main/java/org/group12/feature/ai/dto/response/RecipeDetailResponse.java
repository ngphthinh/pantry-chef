package org.group12.feature.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.recipe.enums.Difficulty;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Recipe detail")
public class RecipeDetailResponse {

    private UUID id;
    private String externalId;      // null nếu là công thức custom
    private String title;
    private String imageUrl;
    private Integer servings;
    private Integer readyInMinutes;
    private Difficulty difficulty;
    private String sourceUrl;
    private boolean custom;
    private String cuisineName;

    private List<IngredientItem> ingredients;
    private List<StepItem> steps;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientItem {
        private String name;
        private Double amount;
        private String unit;        // String để nhận cả enum Unit và text từ AI
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepItem {
        private int stepOrder;
        private String text;
        private Integer timerSeconds;
    }
}