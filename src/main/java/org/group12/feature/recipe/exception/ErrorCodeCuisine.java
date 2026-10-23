package org.group12.feature.recipe.exception;

import lombok.Getter;
import org.group12.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodeCuisine implements BaseErrorCode {
    CUISINE_NOT_FOUND("CUISINE_001", "Cuisine not found", HttpStatus.NOT_FOUND),
    CUISINE_CODE_DUPLICATE("CUISINE_002", "Cuisine code is already taken", HttpStatus.CONFLICT),
    CUISINE_IN_USE("CUISINE_003", "Cuisine is used by existing recipes and cannot be deleted", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeCuisine(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
