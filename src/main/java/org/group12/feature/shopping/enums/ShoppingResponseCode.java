package org.group12.feature.shopping.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.group12.common.enums.ResponseCode;

@Getter
@RequiredArgsConstructor
public enum ShoppingResponseCode implements ResponseCode {
    SHOPPING_LIST_CREATED("SHOPPING_201", "Shopping list created successfully"),
    SHOPPING_LISTS_FETCHED("SHOPPING_200", "Shopping lists fetched successfully"),
    SHOPPING_LIST_FETCHED("SHOPPING_200", "Shopping list fetched successfully"),
    SHOPPING_LIST_UPDATED("SHOPPING_200", "Shopping list updated successfully"),
    SHOPPING_LIST_DELETED("SHOPPING_200", "Shopping list deleted successfully"),
    SHOPPING_ITEM_CREATED("SHOPPING_201", "Shopping item created successfully"),
    SHOPPING_ITEM_UPDATED("SHOPPING_200", "Shopping item updated successfully"),
    SHOPPING_ITEM_DELETED("SHOPPING_200", "Shopping item deleted successfully"),
    SHOPPING_ITEMS_MOVED_TO_PANTRY("SHOPPING_200", "Bought shopping items moved to pantry successfully");

    private final String code;
    private final String message;
}
