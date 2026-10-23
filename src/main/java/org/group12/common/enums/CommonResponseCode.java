package org.group12.common.enums;

import lombok.Getter;

@Getter
public enum CommonResponseCode implements ResponseCode{
    UPLOAD_MEDIA_SUCCESS("200", "Upload media successfully"),
    GET_UNITS_SUCCESS("200", "Get units successfully"),
    GET_ENUMS_SUCCESS("200", "Get enums successfully"),
    CONVERT_UNIT_SUCCESS("200", "Convert unit successfully"),
    GET_STATS_SUCCESS("200", "Get stats successfully");
    private final String  code;
    private final String message;

    CommonResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }


}
