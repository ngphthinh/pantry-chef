package org.group12.feature.cuisine.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.group12.common.enums.ResponseCode;

@Getter
@RequiredArgsConstructor
public enum CuisineResponseCode implements ResponseCode {
    CUISINES_FETCHED("CUISINE_200", "Cuisines fetched successfully"),
    CUISINE_CREATED("CUISINE_201", "Cuisine created successfully"),
    CUISINE_UPDATED("CUISINE_200", "Cuisine updated successfully"),
    CUISINE_DELETED("CUISINE_200", "Cuisine deleted successfully");

    private final String code;
    private final String message;
}
