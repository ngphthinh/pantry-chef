package org.group12.feature.auth.exception;

import org.group12.common.exception.AppException;
import org.group12.common.exception.BaseErrorCode;

public class PasswordInCorrectException extends AppException {
    public PasswordInCorrectException() {
        super(ErrorCodeAuth.PASSWORD_INCORRECT);
    }
}
