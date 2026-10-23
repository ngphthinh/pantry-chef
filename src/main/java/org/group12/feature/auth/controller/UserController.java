// org.group12.feature.auth.controller.UserController
package org.group12.feature.auth.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.common.dto.response.CursorResponse;
import org.group12.feature.auth.dto.response.UserResponse;
import org.group12.feature.auth.dto.request.UserUpdateRequest;
import org.group12.feature.auth.enums.UserResponseCode;
import org.group12.feature.auth.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe() {
        return ApiResponse.<UserResponse>builder()
                .code(UserResponseCode.GET_ME_SUCCESS.getCode())
                .message(UserResponseCode.GET_ME_SUCCESS.getMessage())
                .data(userService.getProfile())
                .build();
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@Valid @RequestBody UserUpdateRequest req) {
        return ApiResponse.<UserResponse>builder()
                .code(UserResponseCode.UPDATE_ME_SUCCESS.getCode())
                .message(UserResponseCode.UPDATE_ME_SUCCESS.getMessage())
                .data(userService.updateProfile(req))
                .build();
    }


    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe() {
        userService.deleteAccount();
    }

    @PutMapping("/me/avatar")
    public ApiResponse<UserResponse> updateAvatar(@RequestPart("avatar") MultipartFile multipartFile) {
        return ApiResponse.<UserResponse>builder()
                .code(UserResponseCode.UPDATE_AVATAR_SUCCESS.getCode())
                .message(UserResponseCode.UPDATE_AVATAR_SUCCESS.getMessage())
                .data(userService.updateAvatar(multipartFile))
                .build();
    }

    @GetMapping("/admin")
    public ApiResponse<CursorResponse<UserResponse>> getAllUser(@RequestParam( required = false) UUID cursor,
                                                                @RequestParam(defaultValue = "10", required = false) @Min(10) @Max(50) int size,
                                                                @RequestParam(required = false) String keyword) {
        return ApiResponse.<CursorResponse<UserResponse>>builder()
                .code(UserResponseCode.GET_ALL_USERS_SUCCESS.getCode())
                .message(UserResponseCode.GET_ALL_USERS_SUCCESS.getMessage())
                .data(userService.getAllUsers(cursor, size, keyword))
                .build();

    }
}