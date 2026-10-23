package org.group12.feature.auth.exception;


import org.group12.common.exception.AppException;
import org.group12.common.exception.ErrorCode;

public class EmailDuplicateException extends AppException {
    public EmailDuplicateException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
}
