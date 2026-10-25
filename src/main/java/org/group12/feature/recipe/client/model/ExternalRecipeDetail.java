package org.group12.feature.recipe.client.model;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalRecipeDetail implements Serializable {

    private String externalId;
    private String title;
    private String imageUrl;
    private Integer servings;
    private Integer readyInMinutes;
    private String sourceUrl;
    private String cuisineName;
    private List<ExternalRecipeIngredient> ingredients;
    private List<ExternalRecipeStep> steps;

    public void setIngredients(List<ExternalRecipeIngredient> ingredients) {
        this.ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
    }

    public void setSteps(List<ExternalRecipeStep> steps) {
        this.steps = steps == null ? List.of() : List.copyOf(steps);
    }
}