package org.group12.feature.ingredient.mapper;

import org.group12.feature.ingredient.dto.response.IngredientCategoryResponse;
import org.group12.feature.ingredient.entity.IngredientCategory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredientCategoryMapper {
    IngredientCategoryResponse toIngredientCategoryResponse(IngredientCategory category);

    List<IngredientCategoryResponse> toIngredientCategoryResponses(List<IngredientCategory> categories);
}
