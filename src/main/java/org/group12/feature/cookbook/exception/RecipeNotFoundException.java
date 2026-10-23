package org.group12.feature.cookbook.exception;

import org.group12.common.exception.AppException;

public class RecipeNotFoundException extends AppException {
    public RecipeNotFoundException() {
        super(ErrorCodeCookbook.RECIPE_NOT_FOUND);
    }
}