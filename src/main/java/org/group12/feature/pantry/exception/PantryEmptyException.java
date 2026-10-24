package org.group12.feature.pantry.exception;

import org.group12.common.exception.AppException;
import org.group12.common.exception.BaseErrorCode;

public class PantryEmptyException extends AppException {
    public PantryEmptyException() {
        super(ErrorCodePantry.PANTRY_EMPTY);
    }
}
