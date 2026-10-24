package org.group12.feature.ai.mapper;

import org.group12.feature.ai.dto.response.AiMealPlanDraft;
import org.group12.feature.ai.dto.response.MealPlanResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MealPlanMapper {

    @Mapping(target = "date", expression = "java(weekStart.plus(item.getDayIndex(), java.time.temporal.ChronoUnit.DAYS))")
    @Mapping(target = "mealType", source = "item.mealType")
    @Mapping(target = "recipeTitle", expression = "java(item.getRecipeTitle().strip())")
    @Mapping(target = "recipeId", source = "recipeId")
    @Mapping(target = "readyInMinutes", source = "item.readyInMinutes")
    MealPlanResponse.Entry toEntry(AiMealPlanDraft.Item item, Instant weekStart, UUID recipeId);

    default MealPlanResponse toMealPlanResponse(Instant weekStart, List<MealPlanResponse.Entry> entries) {
        return MealPlanResponse.builder()
                .weekStart(weekStart)
                .entries(entries)
                .build();
    }
}