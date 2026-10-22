package org.group12.feature.ingredient.exception;

import lombok.Getter;
import org.group12.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodeIngredient implements BaseErrorCode {
    INGREDIENT_NOT_FOUND("INGREDIENT_404", "Ingredient not found", HttpStatus.NOT_FOUND),
    INGREDIENT_CATEGORY_NOT_FOUND("INGREDIENT_CAT_404", "Ingredient category not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeIngredient(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
