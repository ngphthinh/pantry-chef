package org.group12.feature.ingredient.mapper;

import org.group12.feature.ingredient.dto.response.IngredientCategoryResponse;
import org.group12.feature.ingredient.entity.IngredientCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredientCategoryMapper {
    @Mapping(target = "system", expression = "java(category.getOwner() == null)")
    IngredientCategoryResponse toResponse(IngredientCategory category);

    List<IngredientCategoryResponse> toResponses(List<IngredientCategory> categories);
}
