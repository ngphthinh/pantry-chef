package org.group12.feature.recipe.exception;

import org.group12.common.exception.AppException;

public class InvalidServingsException extends AppException {
    public InvalidServingsException() {
        super(ErrorCodeRecipe.RECIPE_INVALID_SERVINGS);
    }
}
