package org.group12.feature.cookbook.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CookbookResponseCode {
    SAVED_RECIPE_CREATE_SUCCESS("1000", "Saved recipe created successfully"),
    SAVED_RECIPE_LIST_SUCCESS("1001", "Saved recipes retrieved successfully"),
    SAVED_RECIPE_GET_SUCCESS("1002", "Saved recipe details retrieved successfully"),
    SAVED_RECIPE_UPDATE_SUCCESS("1003", "Saved recipe updated successfully"),
    COLLECTION_LIST_SUCCESS("1004", "Collections retrieved successfully"),
    COLLECTION_CREATE_SUCCESS("1005", "Collection created successfully"),
    COLLECTION_UPDATE_SUCCESS("1006", "Collection name updated successfully"),
    COLLECTION_RECIPE_ADD_SUCCESS("1007", "Recipe added to collection successfully"),
    COLLECTION_RECIPE_REMOVE_SUCCESS("1008", "Recipe removed from collection successfully"),
    COLLECTION_GET_SUCCESS("1009", "Collection details retrieved successfully"),;

    private final String code;
    private final String message;
}