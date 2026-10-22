package org.group12.feature.ingredient.exception;

import org.group12.common.exception.AppException;

public class IngredientCategoryInUseException extends AppException {
    public IngredientCategoryInUseException() {
        super(ErrorCodeIngredient.CATEGORY_IN_USE);
    }
}
