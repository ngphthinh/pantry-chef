package org.group12.feature.recipe.exception;

import org.group12.common.exception.AppException;

public class InvalidRecipeSearchException extends AppException {
    public InvalidRecipeSearchException() {
        super(ErrorCodeRecipe.RECIPE_INVALID_SEARCH);
    }
}
