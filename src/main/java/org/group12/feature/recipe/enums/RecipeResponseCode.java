package org.group12.feature.recipe.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.group12.common.enums.ResponseCode;

@Getter
@RequiredArgsConstructor
public enum RecipeResponseCode implements ResponseCode {
    RECIPES_FETCHED("RECIPE_200", "Recipes fetched successfully"),
    RECIPE_FETCHED("RECIPE_200", "Recipe fetched successfully"),
    RECIPE_SCALED("RECIPE_200", "Recipe scaled successfully"),
    CUSTOM_RECIPE_CREATED("RECIPE_201", "Custom recipe created successfully"),
    CUSTOM_RECIPES_FETCHED("RECIPE_200", "Custom recipes fetched successfully"),
    CUSTOM_RECIPE_FETCHED("RECIPE_200", "Custom recipe fetched successfully"),
    CUSTOM_RECIPE_UPDATED("RECIPE_200", "Custom recipe updated successfully"),
    CUSTOM_RECIPE_DELETED("RECIPE_200", "Custom recipe deleted successfully"),
    CUSTOM_RECIPE_IMAGE_UPLOADED("RECIPE_200", "Custom recipe image uploaded successfully");

    private final String code;
    private final String message;
}
