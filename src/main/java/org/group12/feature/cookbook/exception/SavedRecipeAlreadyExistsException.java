package org.group12.feature.cookbook.exception;

import org.group12.common.exception.AppException;

public class SavedRecipeAlreadyExistsException extends AppException {
    public SavedRecipeAlreadyExistsException() {
        super(ErrorCodeCookbook.SAVED_RECIPE_ALREADY_EXISTS);
    }
}
