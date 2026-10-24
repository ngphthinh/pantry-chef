package org.group12.feature.pantry.exception;

import lombok.Getter;
import org.group12.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodePantry implements BaseErrorCode {
    PANTRY_ITEM_NOT_FOUND("PANTRY_404", "Pantry item not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_QUANTITY("PANTRY_400", "Insufficient quantity in pantry item", HttpStatus.BAD_REQUEST),
    EMPTY_IMAGE("PANTRY_400_1", "Image file is empty", HttpStatus.BAD_REQUEST),
    PANTRY_EMPTY("PANTRY_400_2", "Pantry is empty", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodePantry(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
