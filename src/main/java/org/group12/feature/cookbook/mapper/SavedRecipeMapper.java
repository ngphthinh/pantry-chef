package org.group12.feature.cookbook.mapper;

import org.group12.feature.cookbook.dto.request.SavedRecipeUpdateRequest;
import org.group12.feature.cookbook.dto.response.SavedRecipeDetailResponse;
import org.group12.feature.cookbook.dto.response.SavedRecipeResponse;
import org.group12.feature.cookbook.entity.SavedRecipe;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SavedRecipeMapper {
    @Mapping(target = "recipeId", source = "recipe.id")
    @Mapping(target = "recipeTitle", source = "recipe.title")
    @Mapping(target = "recipeImageUrl", source = "recipe.imageUrl")
    SavedRecipeResponse toSavedRecipeResponse(SavedRecipe savedRecipe);
    SavedRecipeDetailResponse toSavedRecipeDetailResponse(SavedRecipe savedRecipe);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "recipe", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "note", source = "note", qualifiedByName = "normalizeNote")
    void updateSavedRecipe(SavedRecipeUpdateRequest req, @MappingTarget SavedRecipe saved);

    @Named("normalizeNote")
    default String normalizeNote(String note) {
        if (note == null) return null;            // null: IGNORE sẽ giữ nguyên giá trị cũ
        String trimmed = note.trim();
        return trimmed.isEmpty() ? null : trimmed; // blank → xoá note
    }
}
