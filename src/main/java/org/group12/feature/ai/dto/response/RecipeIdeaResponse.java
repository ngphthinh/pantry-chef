package org.group12.feature.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIdeaResponse {
    private String name;
    private String description;
    private Integer estimatedMinutes;
    private List<String> usedIngredients;
    private List<String> missingIngredients;
    private Double confidence;
}