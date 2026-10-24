package org.group12.feature.recipe.mapper;

import org.group12.feature.recipe.dto.request.RecipeCreateRequest;
import org.group12.feature.recipe.dto.request.RecipeUpdateRequest;
import org.group12.feature.recipe.dto.response.RecipeDetailResponse;
import org.group12.feature.recipe.dto.response.RecipeIngredientResponse;
import org.group12.feature.recipe.dto.response.RecipeStepResponse;
import org.group12.feature.recipe.dto.response.RecipeSummaryResponse;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.entity.RecipeIngredient;
import org.group12.feature.recipe.entity.RecipeStep;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = CuisineMapper.class)
public interface RecipeMapper {

    RecipeDetailResponse toDetailResponse(Recipe recipe);

    RecipeIngredientResponse toIngredientResponse(RecipeIngredient ingredient);

    RecipeStepResponse toStepResponse(RecipeStep step);

    @Mapping(target = "usedIngredientCount", ignore = true)
    @Mapping(target = "missedIngredientCount", ignore = true)
    @Mapping(target = "matchScore", ignore = true)
    @Mapping(target = "missingIngredients", ignore = true)
    RecipeSummaryResponse toSummaryResponse(Recipe recipe);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "publicImgId", ignore = true)
    @Mapping(target = "sourceUrl", ignore = true)
    @Mapping(target = "custom", ignore = true)
    @Mapping(target = "cuisine", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "ingredients", ignore = true)
    @Mapping(target = "steps", ignore = true)
    Recipe toEntity(RecipeCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "publicImgId", ignore = true)
    @Mapping(target = "sourceUrl", ignore = true)
    @Mapping(target = "custom", ignore = true)
    @Mapping(target = "cuisine", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "ingredients", ignore = true)
    @Mapping(target = "steps", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(RecipeUpdateRequest request, @MappingTarget Recipe recipe);
}
