package org.group12.feature.cookbook.exception;

import org.group12.common.exception.AppException;

public class CollectionNameDuplicateException extends AppException {
    public CollectionNameDuplicateException() {
        super(ErrorCodeCookbook.COLLECTION_NAME_DUPLICATE);
    }
}
