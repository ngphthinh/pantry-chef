package org.group12.feature.sysc.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.group12.common.enums.ResponseCode;

@Getter
@RequiredArgsConstructor
public enum SyncResponseCode implements ResponseCode {

    SYNC_STATUS_LIST_SUCCESS("SYNC_200", "Get sync statuses successfully");

    private final String code;
    private final String message;
}
