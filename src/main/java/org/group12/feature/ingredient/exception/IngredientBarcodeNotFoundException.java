package org.group12.feature.ingredient.exception;

import org.group12.common.exception.AppException;

public class IngredientBarcodeNotFoundException extends AppException {
    public IngredientBarcodeNotFoundException() {
        super(ErrorCodeIngredient.INGREDIENT_BARCODE_NOT_FOUND);
    }
}
