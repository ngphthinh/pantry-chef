package org.group12.common.controller;

import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.common.dto.response.MetaEnumsResponse;
import org.group12.common.enums.CommonResponseCode;
import org.group12.common.service.MetaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/v1/meta")
@RequiredArgsConstructor
public class MetaController {

    private final MetaService metaService;

    @GetMapping("/units")
    public ApiResponse<List<String>> getUnits() {
        return ApiResponse.<List<String>>builder()
                .code(CommonResponseCode.GET_UNITS_SUCCESS.getCode())
                .message(CommonResponseCode.GET_UNITS_SUCCESS.getMessage())
                .data(metaService.getUnits())
                .build();
    }

    @GetMapping("/enums")
    public ApiResponse<MetaEnumsResponse> getEnums() {
        return ApiResponse.<MetaEnumsResponse>builder()
                .code(CommonResponseCode.GET_ENUMS_SUCCESS.getCode())
                .message(CommonResponseCode.GET_ENUMS_SUCCESS.getMessage())
                .data(metaService.getEnums())
                .build();
    }
}