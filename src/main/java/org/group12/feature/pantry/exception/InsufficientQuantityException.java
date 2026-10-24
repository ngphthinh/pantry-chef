package org.group12.feature.pantry.exception;

import org.group12.common.exception.AppException;

public class InsufficientQuantityException extends AppException {
    public InsufficientQuantityException() {
        super(ErrorCodePantry.INSUFFICIENT_QUANTITY);
    }
}
