package org.group12.feature.ingredient.exception;

import lombok.Getter;
import org.group12.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodeIngredient implements BaseErrorCode {
    INGREDIENT_NOT_FOUND("INGREDIENT_404", "Ingredient not found", HttpStatus.NOT_FOUND),
    INGREDIENT_BARCODE_NOT_FOUND("INGREDIENT_405", "No ingredient matches this barcode", HttpStatus.NOT_FOUND),
    INGREDIENT_BARCODE_DUPLICATE("INGREDIENT_409", "Barcode is already used by another ingredient", HttpStatus.CONFLICT),

    CATEGORY_NOT_FOUND("INGREDIENT_CATEGORY_404", "Ingredient category not found", HttpStatus.NOT_FOUND),
    CATEGORY_CODE_DUPLICATE("INGREDIENT_CATEGORY_409", "Ingredient category code already exists", HttpStatus.CONFLICT),
    CATEGORY_IN_USE("INGREDIENT_CATEGORY_410", "Ingredient category is still used by ingredients", HttpStatus.CONFLICT),

    EXCLUDED_INGREDIENT_NOT_FOUND("EXCLUDED_INGREDIENT_404", "Excluded ingredient not found", HttpStatus.NOT_FOUND),
    EXCLUDED_INGREDIENT_DUPLICATE("EXCLUDED_INGREDIENT_409", "Ingredient is already in your excluded list", HttpStatus.CONFLICT),

    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeIngredient(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
