package org.group12.feature.cookbook.dto.response;

import java.util.UUID;

public record SavedRecipeResponse(
        UUID id,
        UUID recipeId,
        String recipeTitle,
        Integer rating,
        String note,
        boolean favorite
) {
}
