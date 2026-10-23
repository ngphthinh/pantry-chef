package org.group12.feature.cookbook.dto.response;

import java.util.UUID;

public record CollectionResponse(UUID id, String name, long recipeCount) {
}
