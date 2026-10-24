package org.group12.common.controller;

import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.common.dto.response.UnitConvertResponse;
import org.group12.common.enums.CommonResponseCode;
import org.group12.common.enums.Unit;
import org.group12.common.service.UnitConversionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/units")
@RequiredArgsConstructor
@Validated
public class UnitController {
    private final UnitConversionService service;

    @GetMapping("/convert")
    public ApiResponse<UnitConvertResponse> convert(
            @RequestParam @DecimalMin("0") BigDecimal value,
            @RequestParam Unit from,
            @RequestParam Unit to) {
        return ApiResponse.<UnitConvertResponse>builder()
                .code(CommonResponseCode.CONVERT_UNIT_SUCCESS.getCode())
                .message(CommonResponseCode.CONVERT_UNIT_SUCCESS.getMessage())
                .data(service.convert(value, from, to))
                .build();
    }
}
