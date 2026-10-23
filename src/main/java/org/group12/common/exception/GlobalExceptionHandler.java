package org.group12.common.exception;

import tools.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.dto.response.ErrorResponse;
import org.group12.common.utils.AppUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final AppUtil appUtil;
//    private final MessageUtil messageUtil;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {

        ErrorResponse response = ErrorResponse.builder()
                .status(ErrorCode.UNCATEGORIZED_EXCEPTION.getHttpStatus().value())
                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                .timestamp(Instant.now())
                .error(ex.getMessage())
                .path(request.getRequestURI())
                .build();


        log.error("Uncaught exception at [{}]", request.getRequestURI(), ex);

        return ResponseEntity
                .status(ErrorCode.UNCATEGORIZED_EXCEPTION.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();

        // Field-level errors (đã i18n qua MessageSource gắn vào LocalValidatorFactoryBean, nếu đã config)
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        ));

        // Class-level (object) errors
        ex.getBindingResult()
                .getGlobalErrors()
                .forEach(error ->
                        errors.put(
                                error.getObjectName(),
                                error.getDefaultMessage()
                        ));

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ErrorCode.VALIDATION_FAILED.getMessage(),
                request.getRequestURI(),
                errors
        );

        log.info("Validation failed at [{}]: {}", request.getRequestURI(), errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .status(ex.getErrorCode().getHttpStatus().value())
                .message(ex.getErrorCode().getMessage())
                .error(HttpStatus.valueOf(ex.getErrorCode().getHttpStatus().value()).getReasonPhrase())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        log.info("AppException at [{}]: {}", request.getRequestURI(), ex.getErrorCode().getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        return ResponseEntity.status(ErrorCode.FORBIDDEN.getHttpStatus())
                .body(ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(ErrorCode.FORBIDDEN.getHttpStatus().value())
                        .error(HttpStatus.valueOf(ErrorCode.FORBIDDEN.getHttpStatus().value()).getReasonPhrase())
                        .message(ErrorCode.FORBIDDEN.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    private String mapAttribute(String message, String attributeKey, String attributeValue) {
        if (message.contains("{" + attributeKey + "}")) {
            return message.replace("{" + attributeKey + "}", attributeValue);
        }
        return message;
    }

    private String mapAttributes(String message, Map<String, Object> attributes) {
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            message = mapAttribute(message, entry.getKey(), entry.getValue().toString());
        }
        return message;
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {

        ErrorCode errorCode = ErrorCode.INVALID_PARAMETER_TYPE;

        Class<?> errorTypeClass = e.getRequiredType();

        log.info(
                "MethodArgumentTypeMismatchException at [{}]: parameter={}, value={}, requiredType={}",
                request.getRequestURI(),
                e.getName(),
                e.getValue(),
                errorTypeClass != null ? errorTypeClass.getSimpleName() : "null"
        );

        Set<Class<?>> numberTypes = Set.of(
                Byte.class, Short.class, Integer.class, Long.class,
                Float.class, Double.class,
                byte.class, short.class, int.class, long.class,
                float.class, double.class
        );

        if (errorTypeClass == null) {
            errorTypeClass = e.getParameter().getParameterType();
        }

        if (errorTypeClass == LocalDate.class) {
            errorCode = ErrorCode.INVALID_DATE_FORMAT;
        } else if (numberTypes.contains(errorTypeClass)) {
            errorCode = ErrorCode.INVALID_NUMBER_FORMAT;
        } else if (errorTypeClass.isEnum()) {
            errorCode = ErrorCode.INVALID_ENUM_VALUE;
        }

        String message = errorCode.getMessage();

        if (e.getValue() != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("value", e.getValue());
            params.put("parameter", e.getName());
            params.put("type", errorTypeClass.getSimpleName());

            if (errorTypeClass.isEnum()) {
                String allowedValues = Arrays.stream(errorTypeClass.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                params.put("allowedValues", allowedValues);
            }

            message = mapAttributes(message, params);
        }

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(errorCode.getHttpStatus().value())
                .error(HttpStatus.valueOf(errorCode.getHttpStatus().value()).getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(ParameterizedException.class)
    public ResponseEntity<ErrorResponse> handleParameterizedException(ParameterizedException e, HttpServletRequest request) {

        String message = e.getErrorCode().getMessage();
        // message = messageUtil.get(message);
        Map<String, Object> parameters = e.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            message = mapAttributes(message, parameters);
        }

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(e.getErrorCode().getHttpStatus().value())
                .error(HttpStatus.valueOf(e.getErrorCode().getHttpStatus().value()).getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {

        ErrorCode errorCode = ErrorCode.FILE_SIZE_EXCEEDED;

        ErrorResponse errorResponse = appUtil.generateErrorResponse(request, errorCode);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(errorResponse);

    }

//    @ExceptionHandler(AccountBanException.class)
//    public ResponseEntity<AccountBanErrorResponse> handleAccountLocked(
//            AccountBanException ex, HttpServletRequest request) {
//
//        Instant now = Instant.now();
//        long remainingSeconds = Math.max(
//                Duration.between(now, ex.getLockedUntil()).getSeconds(), 0
//        );
//
//        String message = mapAttribute(
//                ex.getErrorCode().getMessage(),
//                "remainingSeconds",
//                String.valueOf(remainingSeconds)
//        );
//
//        AccountBanErrorResponse error = AccountBanErrorResponse.builder()
//                .timestamp(now)
//                .status(HttpStatus.FORBIDDEN.value())
//                .error(ex.getErrorCode().toString())
//                .message(message)
//                .path(request.getRequestURI())
//                .lockedUntil(ex.getLockedUntil())
//                .remainingSeconds(remainingSeconds)
//                .build();
//
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
//    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        Throwable cause = ex.getCause();
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;
        log.info("HttpMessageNotReadableException at [{}]: {}", request.getRequestURI(), cause != null ? cause.getMessage() : ex.getMessage());
        String message;

        while (cause != null && !(cause instanceof InvalidFormatException)) {
            cause = cause.getCause();
        }

        if (cause instanceof InvalidFormatException ife
                && ife.getTargetType() != null
                && ife.getTargetType().isEnum()) {

            String fieldName = ife.getPath().isEmpty()
                    ? "field"
                    : ife.getPath().get(ife.getPath().size() - 1).getPropertyName();

            String accepted = Arrays.stream(ife.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            message = mapAttributes(
                    "Invalid value ''{value}'' for field ''{field}''. Accepted values: {accepted}",
                    Map.of(
                            "field", fieldName,
                            "value", String.valueOf(ife.getValue()),
                            "accepted", accepted
                    )
            );
        } else {
            message = errorCode.getMessage();
        }

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(errorCode.getHttpStatus().value())
                .error(HttpStatus.valueOf(errorCode.getHttpStatus().value()).getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();


        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
}