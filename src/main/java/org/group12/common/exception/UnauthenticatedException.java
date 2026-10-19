package org.group12.common.exception;


public class UnauthenticatedException extends AppException {
    public UnauthenticatedException() {
        super(ErrorCode.UNAUTHENTICATED);
    }
}
