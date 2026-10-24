package org.group12.feature.cookbook.exception;

import org.group12.common.exception.AppException;

public class SavedRecipeNotFoundException extends AppException {
    public SavedRecipeNotFoundException() {
        super(ErrorCodeCookbook.SAVED_RECIPE_NOT_FOUND);
    }
}