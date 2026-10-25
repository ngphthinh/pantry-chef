package org.group12.feature.recipe.dto.projection;

import java.util.UUID;

public interface MissingRow {
    UUID getRecipeId();
    String getName();
}