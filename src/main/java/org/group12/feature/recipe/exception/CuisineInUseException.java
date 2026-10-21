package org.group12.feature.recipe.exception;

import org.group12.common.exception.AppException;

public class CuisineInUseException extends AppException {
    public CuisineInUseException() {
        super(ErrorCodeCuisine.CUISINE_IN_USE);
    }
}
