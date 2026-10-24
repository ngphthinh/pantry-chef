package org.group12.feature.ingredient.mapper;

import org.group12.feature.ingredient.dto.response.IngredientResponse;
import org.group12.feature.ingredient.entity.Ingredient;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = IngredientCategoryMapper.class)
public interface IngredientMapper {
    IngredientResponse toIngredientResponse(Ingredient ingredient);
}
