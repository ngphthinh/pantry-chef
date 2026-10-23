package org.group12.feature.auth.exception;

import org.group12.common.exception.AppException;

public class RoleNotFoundException extends AppException {
    public RoleNotFoundException() {
        super(ErrorCodeAuth.ROLE_NOT_FOUND);
    }
}
