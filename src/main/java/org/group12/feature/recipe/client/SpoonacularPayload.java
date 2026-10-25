package org.group12.feature.recipe.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public final class SpoonacularPayload {

    private SpoonacularPayload() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SearchResponse {
        private List<SearchItem> results;
        private Integer totalResults;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SearchItem {
        private Long id;
        private String title;
        private String image;
        private Integer readyInMinutes;
        private Integer servings;
        private List<String> cuisines;
        private Integer usedIngredientCount;
        private Integer missedIngredientCount;
        private List<NamedItem> missedIngredients;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class NamedItem {
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class RandomResponse {
        private List<Information> recipes;
    }

    /**
     * Dùng cho /recipes/{id}/information và từng phần tử của /recipes/random.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Information {
        private Long id;
        private String title;
        private String image;
        private Integer servings;
        private Integer readyInMinutes;
        private String sourceUrl;
        private List<String> cuisines;
        private List<ExtendedIngredient> extendedIngredients;
        private List<Instruction> analyzedInstructions;
        private String instructions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ExtendedIngredient {
        private String name;
        private String original;
        private Double amount;
        private String unit;
        private Measures measures;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Measures {
        private Measure metric;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Measure {
        private Double amount;
        private String unitShort;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Instruction {
        private List<InstructionStep> steps;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class InstructionStep {
        private Integer number;
        private String step;
        private StepLength length;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class StepLength {
        private Integer number;
        private String unit;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SimilarItem {
        private Long id;
        private String title;
        private String imageType;
        private Integer readyInMinutes;
        private Integer servings;
    }
}