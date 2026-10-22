package org.group12.feature.pantry.exception;

import org.group12.common.exception.AppException;

public class PantryItemNotFoundException extends AppException {
    public PantryItemNotFoundException() {
        super(ErrorCodePantry.PANTRY_ITEM_NOT_FOUND);
    }
}
