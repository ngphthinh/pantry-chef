package org.group12.feature.shopping.exception;

import org.group12.common.exception.AppException;

public class NothingToBuyException extends AppException {
    public NothingToBuyException() {
        super(ErrorCodeShopping.SHOPPING_NOTHING_TO_BUY);
    }
}
