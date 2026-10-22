package org.group12.feature.pantry.mapper;

import org.group12.feature.pantry.dto.request.PantryItemCreateRequest;
import org.group12.feature.pantry.dto.request.PantryItemUpdateRequest;
import org.group12.feature.pantry.dto.response.PantryItemResponse;
import org.group12.feature.pantry.entity.PantryItem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PantryItemMapper {

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    @Mapping(target = "categoryId", source = "ingredient.category.id")
    @Mapping(target = "categoryName", source = "ingredient.category.name")
    PantryItemResponse toResponse(PantryItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    PantryItem toEntity(PantryItemCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(PantryItemUpdateRequest request, @MappingTarget PantryItem item);
}
