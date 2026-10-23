package org.group12.feature.auth.exception;

import org.group12.common.exception.AppException;
import org.group12.common.exception.ErrorCode;

public class TokenExpiredException extends AppException {
    public TokenExpiredException() {
        super(ErrorCode.TOKEN_EXPIRED);
    }
}
