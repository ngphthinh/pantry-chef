package org.group12.feature.shopping.exception;

import lombok.Getter;
import org.group12.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodeShopping implements BaseErrorCode {
    SHOPPING_LIST_NOT_FOUND("SHOPPING_001", "Shopping list not found", HttpStatus.NOT_FOUND),
    SHOPPING_ITEM_NOT_FOUND("SHOPPING_002", "Shopping item not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeShopping(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
