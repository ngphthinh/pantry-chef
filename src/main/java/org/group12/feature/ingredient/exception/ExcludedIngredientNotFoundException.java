package org.group12.feature.ingredient.exception;

import org.group12.common.exception.AppException;

public class ExcludedIngredientNotFoundException extends AppException {
    public ExcludedIngredientNotFoundException() {
        super(ErrorCodeIngredient.EXCLUDED_INGREDIENT_NOT_FOUND);
    }
}
