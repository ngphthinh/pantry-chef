package org.group12.feature.recipe.exception;

import org.group12.common.exception.AppException;

public class RecipeNotFoundException extends AppException {
    public RecipeNotFoundException() {
        super(ErrorCodeRecipe.RECIPE_NOT_FOUND);
    }
}
