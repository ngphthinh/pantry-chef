package org.group12.feature.recipe.exception;

import lombok.Getter;
import org.group12.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodeRecipe implements BaseErrorCode {
    RECIPE_NOT_FOUND("RECIPE_404", "Recipe not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeRecipe(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
