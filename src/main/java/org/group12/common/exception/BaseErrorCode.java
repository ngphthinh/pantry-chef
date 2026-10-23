package org.group12.common.exception;

import org.springframework.http.HttpStatusCode;

public interface BaseErrorCode {
    String getMessage();

    HttpStatusCode getHttpStatus();

    String toString();
}