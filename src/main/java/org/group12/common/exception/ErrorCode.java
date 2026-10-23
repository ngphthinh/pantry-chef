package org.group12.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements BaseErrorCode{

    UNAUTHENTICATED("AUTH_001", "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("AUTH_002", "You do not have permission to perform this action", HttpStatus.FORBIDDEN),
    GENERATE_TOKEN_FAILED("AUTH_003", "Failed to generate token", HttpStatus.INTERNAL_SERVER_ERROR),
    INTROSPECT_FAILED("AUTH_004", "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("USER_001", "User not found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("USER_002", "Email is already taken", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("USER_003", "Email or password is incorrect", HttpStatus.UNAUTHORIZED),
    ACCOUNT_BANNED("USER_004", "Your account has been banned", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND("ROLE_001", "Role not found", HttpStatus.NOT_FOUND),
    INVALID_REFRESH_TOKEN("AUTH_005", "Refresh token is invalid or has expired", HttpStatus.UNAUTHORIZED),
    UNCATEGORIZED_EXCEPTION("GEN_001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED("VAL_001", "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER_TYPE("VAL_002", "Invalid parameter type", HttpStatus.BAD_REQUEST),
    INVALID_DATE_FORMAT("VAL_003", "Invalid date format", HttpStatus.BAD_REQUEST),
    INVALID_NUMBER_FORMAT("VAL_004", "Invalid number format", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED("VAL_005", "File size exceeded the limit", HttpStatus.BAD_REQUEST),
    INVALID_ENUM_VALUE("VAL_006", "Invalid enum value for parameter '{parameter}': '{value}'. Expected one of {allowedValues}.", HttpStatus.BAD_REQUEST),
    FORBIDDEN("GEN_002", "Forbidden", HttpStatus.FORBIDDEN),
    BAD_REQUEST("GEN_003", "Bad request", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("AUTH_006", "Token has expired", HttpStatus.UNAUTHORIZED),
    USER_NOT_SUPPORTED("USER_008", "Unsupported user type", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_UUID("VAL_007", "Invalid UUID format", HttpStatus.BAD_REQUEST),
    UPLOAD_MEDIA_FAILED("MEDIA_001", "Upload media failed", HttpStatus.INTERNAL_SERVER_ERROR),
    LOAD_PROMPT_FAILED("PROMPT_001", "Load prompt failed", HttpStatus.INTERNAL_SERVER_ERROR),
    CONTENT_BLOCKED("GEN_004", "Nội dung bị chặn", HttpStatus.UNPROCESSABLE_ENTITY),
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}