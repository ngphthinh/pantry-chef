package org.group12.feature.recipe.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Các kiểu JSON trả về từ Spoonacular. Chỉ khai báo những trường đang dùng. */
final class SpoonacularPayload {

    private SpoonacularPayload() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SearchResponse(List<SearchItem> results, Integer totalResults) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SearchItem(Long id, String title, String image, Integer readyInMinutes, Integer servings,
                      List<String> cuisines, Integer usedIngredientCount, Integer missedIngredientCount,
                      List<NamedItem> missedIngredients) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NamedItem(String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RandomResponse(List<Information> recipes) {
    }

    /** Dùng cho /recipes/{id}/information và từng phần tử của /recipes/random. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Information(Long id, String title, String image, Integer servings, Integer readyInMinutes,
                       String sourceUrl, List<String> cuisines, List<ExtendedIngredient> extendedIngredients,
                       List<Instruction> analyzedInstructions, String instructions) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ExtendedIngredient(String name, String original, Double amount, String unit, Measures measures) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Measures(Measure metric) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Measure(Double amount, String unitShort) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Instruction(List<InstructionStep> steps) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record InstructionStep(Integer number, String step, StepLength length) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record StepLength(Integer number, String unit) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SimilarItem(Long id, String title, String imageType, Integer readyInMinutes, Integer servings) {
    }
}
