package org.group12.feature.shopping.exception;

import org.group12.common.exception.AppException;

public class ShoppingListNotFoundException extends AppException {
    public ShoppingListNotFoundException() {
        super(ErrorCodeShopping.SHOPPING_LIST_NOT_FOUND);
    }
}
