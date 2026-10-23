package org.group12.feature.cookbook.dto.response;

import org.group12.feature.recipe.dto.response.RecipeDetailResponse;

import java.util.UUID;

public record SavedRecipeDetailResponse(
        UUID id,
        Integer rating,
        String note,
        boolean favorite,
        RecipeDetailResponse recipe
) {}
