package org.group12.feature.ai.mapper;

import org.group12.feature.ai.dto.response.AiMealPlanDraft;
import org.group12.feature.ai.dto.response.MealPlanResponse;
import org.group12.feature.recipe.entity.Recipe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AiMealPlanMapper {

    @Mapping(target = "date",
            expression = "java(weekStart.plus(item.getDayIndex(), java.time.temporal.ChronoUnit.DAYS))")
    @Mapping(target = "mealType", source = "item.mealType")
    @Mapping(target = "recipeId", source = "recipe.id")
    @Mapping(target = "recipeTitle", source = "recipe.title")
    @Mapping(target = "servings", constant = "1")
    MealPlanResponse.Entry toEntry(AiMealPlanDraft.Item item, Instant weekStart, Recipe recipe);

    default MealPlanResponse toMealPlanResponse(Instant weekStart, List<MealPlanResponse.Entry> entries) {
        return MealPlanResponse.builder().weekStart(weekStart).entries(entries).build();
    }
}