package org.group12.feature.sysc.controller;

import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.feature.sysc.dto.response.SyncStatusResponse;
import org.group12.feature.sysc.enums.SyncResponseCode;
import org.group12.feature.sysc.service.SyncStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/sync-statuses")
@RequiredArgsConstructor
public class SyncStatusController {

    private final SyncStatusService syncStatusService;

    // GET /v1/sync-statuses
    @GetMapping
    public ApiResponse<List<SyncStatusResponse>> getAll() {
        return ApiResponse.<List<SyncStatusResponse>>builder()
                .code(SyncResponseCode.SYNC_STATUS_LIST_SUCCESS.getCode())
                .message(SyncResponseCode.SYNC_STATUS_LIST_SUCCESS.getMessage())
                .data(syncStatusService.getAll())
                .build();
    }
}
