package org.group12.feature.recipe.exception;

import lombok.Getter;
import org.group12.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodeRecipe implements BaseErrorCode {
    RECIPE_NOT_FOUND("RECIPE_404", "Recipe not found", HttpStatus.NOT_FOUND),
    RECIPE_INVALID_CURSOR("RECIPE_001", "Cursor must be a non-negative integer offset", HttpStatus.BAD_REQUEST),
    RECIPE_INVALID_SEARCH("RECIPE_002",
            "Provide at least one ingredient or a keyword, and maxMinutes must be positive", HttpStatus.BAD_REQUEST),
    RECIPE_INVALID_SERVINGS("RECIPE_003",
            "Servings must be a positive number and the recipe must define its base servings", HttpStatus.UNPROCESSABLE_ENTITY),
    RECIPE_PROVIDER_ERROR("RECIPE_004", "Recipe provider is unavailable or returned an error", HttpStatus.BAD_GATEWAY),
    RECIPE_PROVIDER_QUOTA_EXCEEDED("RECIPE_005", "Recipe provider quota exceeded, please try again later", HttpStatus.TOO_MANY_REQUESTS);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeRecipe(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
