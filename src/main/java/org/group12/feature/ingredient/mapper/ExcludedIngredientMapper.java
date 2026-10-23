package org.group12.feature.ingredient.mapper;

import org.group12.feature.ingredient.dto.response.ExcludedIngredientResponse;
import org.group12.feature.ingredient.entity.ExcludedIngredient;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExcludedIngredientMapper {
    ExcludedIngredientResponse toExcludedIngredientResponse(ExcludedIngredient excludedIngredient);

    List<ExcludedIngredientResponse> toExcludedIngredientResponses(List<ExcludedIngredient> excludedIngredients);
}
