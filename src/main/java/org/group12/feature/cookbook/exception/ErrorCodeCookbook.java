package org.group12.feature.cookbook.exception;

import lombok.Getter;
import org.group12.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodeCookbook implements BaseErrorCode {
    RECIPE_NOT_FOUND("COOKBOOK_001", "Recipe not found", HttpStatus.NOT_FOUND),
    SAVED_RECIPE_NOT_FOUND("COOKBOOK_002", "Saved recipe not found", HttpStatus.NOT_FOUND),
    COLLECTION_NAME_DUPLICATE("COOKBOOK_003", "Collection name already exists", HttpStatus.CONFLICT),
    COLLECTION_NOT_FOUND("COOKBOOK_004", "Collection not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeCookbook(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}