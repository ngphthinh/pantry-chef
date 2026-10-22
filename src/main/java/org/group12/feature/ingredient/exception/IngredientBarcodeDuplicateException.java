package org.group12.feature.ingredient.exception;

import org.group12.common.exception.AppException;

public class IngredientBarcodeDuplicateException extends AppException {
    public IngredientBarcodeDuplicateException() {
        super(ErrorCodeIngredient.INGREDIENT_BARCODE_DUPLICATE);
    }
}
