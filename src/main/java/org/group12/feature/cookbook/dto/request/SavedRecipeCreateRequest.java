package org.group12.feature.cookbook.dto.request;

import jakarta.validation.constraints.AssertTrue;

import java.util.UUID;

public record SavedRecipeCreateRequest(String externalId, UUID recipeId) {
    @AssertTrue(message = "Phải truyền đúng một trong hai: externalId hoặc recipeId")
    public boolean isExactlyOneIdentifier() {
        boolean hasExternal = externalId != null && !externalId.isBlank();
        return hasExternal ^ (recipeId != null);
    }
}
