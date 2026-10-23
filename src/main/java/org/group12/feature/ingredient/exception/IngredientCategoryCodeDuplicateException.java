package org.group12.feature.ingredient.exception;

import org.group12.common.exception.AppException;

public class IngredientCategoryCodeDuplicateException extends AppException {
    public IngredientCategoryCodeDuplicateException() {
        super(ErrorCodeIngredient.CATEGORY_CODE_DUPLICATE);
    }
}
