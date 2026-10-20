package org.group12.feature.shopping.exception;

import org.group12.common.exception.AppException;

public class ShoppingItemNotFoundException extends AppException {
    public ShoppingItemNotFoundException() {
        super(ErrorCodeShopping.SHOPPING_ITEM_NOT_FOUND);
    }
}
