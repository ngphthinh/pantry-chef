package org.group12.feature.cookbook.exception;

import org.group12.common.exception.AppException;

public class CollectionNotFoundException extends AppException {
    public CollectionNotFoundException() {
        super(ErrorCodeCookbook.COLLECTION_NOT_FOUND);
    }
}
