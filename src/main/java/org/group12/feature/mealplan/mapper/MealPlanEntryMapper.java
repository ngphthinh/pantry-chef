package org.group12.feature.mealplan.mapper;

import org.group12.feature.mealplan.dto.request.MealPlanEntryCreateRequest;
import org.group12.feature.mealplan.dto.request.MealPlanEntryUpdateRequest;
import org.group12.feature.mealplan.dto.response.MealPlanEntryResponse;
import org.group12.feature.mealplan.entity.MealPlanEntry;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MealPlanEntryMapper {

    @Mapping(target = "recipeId", source = "recipe.id")
    @Mapping(target = "recipeTitle", source = "recipe.title")
    @Mapping(target = "recipeImageUrl", source = "recipe.imageUrl")
    MealPlanEntryResponse toResponse(MealPlanEntry entry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mealPlan", ignore = true)
    @Mapping(target = "recipe", ignore = true)
    @Mapping(target = "cooked", constant = "false")
    MealPlanEntry toEntity(MealPlanEntryCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mealPlan", ignore = true)
    @Mapping(target = "recipe", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(MealPlanEntryUpdateRequest request, @MappingTarget MealPlanEntry entry);
}
