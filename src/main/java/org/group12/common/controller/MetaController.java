package org.group12.common.controller;

import org.group12.common.dto.response.ApiResponse;
import org.group12.common.dto.response.MetaEnumsResponse;
import org.group12.common.enums.CommonResponseCode;
import org.group12.common.enums.Unit;
import org.group12.feature.auth.enums.MeasurementSystem;
import org.group12.feature.auth.enums.ThemeMode;
import org.group12.feature.mealplan.enums.MealType;
import org.group12.feature.pantry.enums.StorageLocation;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.shopping.enums.ShoppingItemStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/v1/meta")
public class MetaController {
    @GetMapping("/units")
    public ApiResponse<List<String>> getUnits() {
        return ApiResponse.<List<String>>builder()
                .code(CommonResponseCode.GET_UNITS_SUCCESS.getCode())
                .message(CommonResponseCode.GET_UNITS_SUCCESS.getMessage())
                .data(Arrays.stream(Unit.values()).map(Unit::name).toList())
                .build();
    }

    @GetMapping("/enums")
    public ApiResponse<MetaEnumsResponse> getEnums() {
        return ApiResponse.<MetaEnumsResponse>builder()
                .code(CommonResponseCode.GET_ENUMS_SUCCESS.getCode())
                .message(CommonResponseCode.GET_ENUMS_SUCCESS.getMessage())
                .data(new MetaEnumsResponse(
                        names(Unit.class),
                        names(MealType.class),
                        names(Difficulty.class),
                        names(StorageLocation.class),
                        names(ShoppingItemStatus.class),
                        names(MeasurementSystem.class),
                        names(ThemeMode.class)))
                .build();
    }

    private static <E extends Enum<E>> List<String> names(Class<E> type) {
        return Arrays.stream(type.getEnumConstants()).map(Enum::name).toList();
    }
}
