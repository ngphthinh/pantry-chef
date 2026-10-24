package org.group12.feature.ai.mapper;

import org.group12.feature.ai.dto.response.AiAdaptedRecipe;
import org.group12.feature.ai.dto.response.RecipeDetailResponse;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.entity.RecipeIngredient;
import org.group12.feature.recipe.entity.RecipeStep;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdaptRecipeMapper {

    @Mapping(target = "cuisineName", source = "cuisine.name")
    RecipeDetailResponse toDetail(Recipe recipe);

    RecipeDetailResponse.IngredientItem toIngredientItem(RecipeIngredient src);

    RecipeDetailResponse.StepItem toStepItem(RecipeStep src);

    RecipeDetailResponse.IngredientItem toIngredientItem(AiAdaptedRecipe.Ingredient src);

    @Mapping(target = "stepOrder", ignore = true)
    @Mapping(target = "timerSeconds", ignore = true)
    RecipeDetailResponse.StepItem toStepItem(AiAdaptedRecipe.Step src);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "ingredients", source = "adapted.ingredients")
    @Mapping(target = "steps", source = "adapted.steps")
    void applyAdapted(AiAdaptedRecipe adapted, @MappingTarget RecipeDetailResponse target);

    @AfterMapping
    default void renumber(@MappingTarget RecipeDetailResponse target) {
        List<RecipeDetailResponse.StepItem> steps = target.getSteps();
        if (steps == null) return;
        for (int i = 0; i < steps.size(); i++) {
            steps.get(i).setStepOrder(i + 1);
        }
    }
}