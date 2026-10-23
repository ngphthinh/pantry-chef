package org.group12.common.stats.controller;

import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.common.enums.CommonResponseCode;
import org.group12.common.stats.dto.response.UserStatsResponse;
import org.group12.feature.stats.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService service;

    @GetMapping("/me")
    public ApiResponse<UserStatsResponse> getMe() {
        return ApiResponse.<UserStatsResponse>builder()
                .code(CommonResponseCode.GET_STATS_SUCCESS.getCode())
                .message(CommonResponseCode.GET_STATS_SUCCESS.getMessage())
                .data(service.getMyStats())
                .build();
    }
}