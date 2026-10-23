package org.group12.feature.ingredient.exception;

import org.group12.common.exception.AppException;

public class IngredientCategoryNotFoundException extends AppException {
    public IngredientCategoryNotFoundException() {
        super(ErrorCodeIngredient.CATEGORY_NOT_FOUND);
    }
}
