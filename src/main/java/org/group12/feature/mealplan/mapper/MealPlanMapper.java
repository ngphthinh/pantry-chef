package org.group12.feature.mealplan.mapper;

import org.group12.feature.mealplan.dto.request.MealPlanCreateRequest;
import org.group12.feature.mealplan.dto.response.MealPlanResponse;
import org.group12.feature.mealplan.entity.MealPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MealPlanEntryMapper.class})
public interface MealPlanMapper {

    MealPlanResponse toResponse(MealPlan mealPlan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "entries", ignore = true)
    MealPlan toEntity(MealPlanCreateRequest request);
}
