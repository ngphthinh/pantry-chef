package org.group12.feature.recipe.client.model;

import java.io.Serializable;
import java.util.List;

public record ExternalRecipeDetail(
        String externalId,
        String title,
        String imageUrl,
        Integer servings,
        Integer readyInMinutes,
        String sourceUrl,
        String cuisineName,
        List<ExternalRecipeIngredient> ingredients,
        List<ExternalRecipeStep> steps
) implements Serializable {

    public ExternalRecipeDetail {
        ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
        steps = steps == null ? List.of() : List.copyOf(steps);
    }
}
