package org.group12.feature.cookbook.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.recipe.dto.response.RecipeDetailResponse;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedRecipeDetailResponse {
    private UUID id;
    private Integer rating;
    private String note;
    private boolean favorite;
    private RecipeDetailResponse recipe;
}