package org.group12.feature.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAdaptedRecipe {
    private List<Ingredient> ingredients;
    private List<Step> steps;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ingredient {
        private String name;
        private Double amount;
        private String unit;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Step {
        private String text;
    }
}