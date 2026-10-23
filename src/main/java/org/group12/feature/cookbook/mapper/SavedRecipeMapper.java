package org.group12.feature.cookbook.mapper;

import org.group12.feature.cookbook.dto.response.SavedRecipeDetailResponse;
import org.group12.feature.cookbook.dto.response.SavedRecipeResponse;
import org.group12.feature.cookbook.entity.SavedRecipe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SavedRecipeMapper {
    @Mapping(target = "recipeId", source = "recipe.id")
    @Mapping(target = "recipeTitle", source = "recipe.title")
    SavedRecipeResponse toResponse(SavedRecipe savedRecipe);
    SavedRecipeDetailResponse toDetailResponse(SavedRecipe savedRecipe);
}
