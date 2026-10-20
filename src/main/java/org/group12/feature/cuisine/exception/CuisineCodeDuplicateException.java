package org.group12.feature.cuisine.exception;

import org.group12.common.exception.AppException;

public class CuisineCodeDuplicateException extends AppException {
    public CuisineCodeDuplicateException() {
        super(ErrorCodeCuisine.CUISINE_CODE_DUPLICATE);
    }
}
