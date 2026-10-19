package org.group12.feature.auth.exception;

import lombok.Getter;
import org.group12.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodeAuth implements BaseErrorCode {
    ACCOUNT_BANNED("AUTH_403", "Your account has been banned", HttpStatus.FORBIDDEN),
    ACCOUNT_NOT_FOUND("AUTH_404", "Account not found", HttpStatus.NOT_FOUND),
    ACCOUNT_LOCKED("AUTH_423", "Your account is locked", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND("AUTH_405", "Role not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodeAuth(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
