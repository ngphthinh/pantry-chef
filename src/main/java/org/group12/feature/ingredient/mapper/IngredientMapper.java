package org.group12.feature.ingredient.mapper;

import org.group12.feature.ingredient.dto.response.IngredientResponse;
import org.group12.feature.ingredient.entity.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = IngredientCategoryMapper.class)
public interface IngredientMapper {
    @Mapping(target = "system", expression = "java(ingredient.getOwner() == null)")
    IngredientResponse toIngredientResponse(Ingredient ingredient);
}
