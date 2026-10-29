package org.group12.feature.cookbook.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionResponseDetail {
    private UUID id;
    private String name;
    private long recipeCount;
    List<SavedRecipeResponse> recipes;
}
