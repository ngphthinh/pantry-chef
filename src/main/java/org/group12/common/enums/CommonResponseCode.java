package org.group12.common.enums;

import lombok.Getter;

@Getter
public enum CommonResponseCode implements ResponseCode{
    UPLOAD_MEDIA_SUCCESS("200", "Upload media successfully"),;
    private final String  code;
    private final String message;

    CommonResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }


}
