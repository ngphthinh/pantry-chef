package org.group12.feature.ai.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.group12.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum AiErrorCode implements BaseErrorCode {
    IMAGE_UPLOAD_FAILED("AI_400", "Image upload failed", HttpStatus.BAD_REQUEST),
    IMAGE_GENERATION_FAILED("AI_500", "Image generation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_IMAGE_FORMAT("AI_400", "Invalid image format", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_IMAGE_TYPE("AI_400", "Unsupported image type", HttpStatus.BAD_REQUEST),
    IMAGE_TOO_LARGE("AI_400", "Image size exceeds the limit", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_FOUND("AI_404", "Image not found", HttpStatus.NOT_FOUND),
    AI_PROVIDER_ERROR("AI_500", "AI provider error", HttpStatus.BAD_GATEWAY),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
