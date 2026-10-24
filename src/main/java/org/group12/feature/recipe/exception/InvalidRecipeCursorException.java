package org.group12.feature.recipe.exception;

import org.group12.common.exception.AppException;

public class InvalidRecipeCursorException extends AppException {
    public InvalidRecipeCursorException() {
        super(ErrorCodeRecipe.RECIPE_INVALID_CURSOR);
    }
}
