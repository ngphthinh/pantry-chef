package org.group12.feature.ingredient.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.group12.common.enums.ResponseCode;

@Getter
@RequiredArgsConstructor
public enum IngredientResponseCode implements ResponseCode {

    INGREDIENT_SEARCH_SUCCESS("INGREDIENT_200", "Search ingredients successfully"),
    INGREDIENT_GET_SUCCESS("INGREDIENT_200", "Get ingredient successfully"),
    INGREDIENT_BARCODE_SUCCESS("INGREDIENT_200", "Get ingredient by barcode successfully"),
    INGREDIENT_CREATE_SUCCESS("INGREDIENT_201", "Create ingredient successfully"),
    INGREDIENT_UPDATE_SUCCESS("INGREDIENT_200", "Update ingredient successfully"),
    INGREDIENT_DELETE_SUCCESS("INGREDIENT_200", "Delete ingredient successfully"),

    CATEGORY_LIST_SUCCESS("INGREDIENT_CATEGORY_200", "Get ingredient categories successfully"),
    CATEGORY_CREATE_SUCCESS("INGREDIENT_CATEGORY_201", "Create ingredient category successfully"),
    CATEGORY_UPDATE_SUCCESS("INGREDIENT_CATEGORY_200", "Update ingredient category successfully"),
    CATEGORY_DELETE_SUCCESS("INGREDIENT_CATEGORY_200", "Delete ingredient category successfully"),

    EXCLUDED_LIST_SUCCESS("EXCLUDED_INGREDIENT_200", "Get excluded ingredients successfully"),
    EXCLUDED_CREATE_SUCCESS("EXCLUDED_INGREDIENT_201", "Add excluded ingredient successfully"),
    EXCLUDED_DELETE_SUCCESS("EXCLUDED_INGREDIENT_200", "Remove excluded ingredient successfully");

    private final String code;
    private final String message;
}
