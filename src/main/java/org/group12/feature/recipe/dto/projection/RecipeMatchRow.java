package org.group12.feature.recipe.dto.projection;

import java.util.UUID;

public interface RecipeMatchRow {
    UUID getRecipeId();
    long getUsed();
    long getTotal();
}
