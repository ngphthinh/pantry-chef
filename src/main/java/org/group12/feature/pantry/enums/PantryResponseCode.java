package org.group12.feature.pantry.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.group12.common.enums.ResponseCode;

@Getter
@RequiredArgsConstructor
public enum PantryResponseCode implements ResponseCode {
    PANTRY_ITEM_CREATED("PANTRY_201", "Pantry item created successfully"),
    PANTRY_ITEMS_CREATED("PANTRY_201", "Pantry items batch created successfully"),
    PANTRY_ITEMS_FETCHED("PANTRY_200", "Pantry items fetched successfully"),
    PANTRY_ITEM_FETCHED("PANTRY_200", "Pantry item fetched successfully"),
    PANTRY_ITEM_UPDATED("PANTRY_200", "Pantry item updated successfully"),
    PANTRY_ITEM_DELETED("PANTRY_200", "Pantry item deleted successfully"),
    PANTRY_ITEM_PHOTO_UPLOADED("PANTRY_200", "Pantry item photo uploaded successfully"),
    PANTRY_ITEM_CONSUMED("PANTRY_200", "Pantry item consumed successfully");

    private final String code;
    private final String message;
}
