package org.group12.feature.recipe.exception;

import org.group12.common.exception.AppException;

public class CuisineNotFoundException extends AppException {
    public CuisineNotFoundException() {
        super(ErrorCodeCuisine.CUISINE_NOT_FOUND);
    }
}
