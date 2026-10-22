package org.group12.feature.ingredient.exception;

import org.group12.common.exception.AppException;

public class IngredientNotFoundException extends AppException {
    public IngredientNotFoundException() {
        super(ErrorCodeIngredient.INGREDIENT_NOT_FOUND);
    }
}
