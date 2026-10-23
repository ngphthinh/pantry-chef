package org.group12.common.exception;

public class AccessDeniedException extends AppException{
    public AccessDeniedException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
