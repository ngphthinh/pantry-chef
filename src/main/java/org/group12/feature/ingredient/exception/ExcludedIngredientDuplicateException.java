package org.group12.feature.ingredient.exception;

import org.group12.common.exception.AppException;

public class ExcludedIngredientDuplicateException extends AppException {
    public ExcludedIngredientDuplicateException() {
        super(ErrorCodeIngredient.EXCLUDED_INGREDIENT_DUPLICATE);
    }
}
