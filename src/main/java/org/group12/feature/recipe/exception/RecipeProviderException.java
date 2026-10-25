package org.group12.feature.recipe.exception;

import org.group12.common.exception.AppException;

public class RecipeProviderException extends AppException {
    public RecipeProviderException() {
        super(ErrorCodeRecipe.RECIPE_PROVIDER_ERROR);
    }
}
