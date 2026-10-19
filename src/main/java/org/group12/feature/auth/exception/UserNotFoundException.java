package org.group12.feature.auth.exception;


import org.group12.common.exception.AppException;
import org.group12.common.exception.ErrorCode;

public class UserNotFoundException extends AppException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
