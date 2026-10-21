package org.group12.feature.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.group12.common.enums.ResponseCode;

@Getter
@RequiredArgsConstructor
public enum UserResponseCode implements ResponseCode {

    GET_ME_SUCCESS("USER_200", "Get user info successfully"),

    UPDATE_ME_SUCCESS("USER_201", "Update user info successfully"),
    UPDATE_AVATAR_SUCCESS("USER_202", "Update avatar successfully"),
    GET_ALL_USERS_SUCCESS("USER_203", "Get all users successfully");

    private final String code;
    private final String message;
}
