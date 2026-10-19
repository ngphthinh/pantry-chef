package org.group12.feature.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.feature.auth.dto.request.LoginRequest;
import org.group12.feature.auth.dto.request.LogoutRequest;
import org.group12.feature.auth.dto.request.RefreshTokenRequest;
import org.group12.feature.auth.dto.request.UserCreateRequest;
import org.group12.feature.auth.dto.response.AuthenticateResponse;
import org.group12.feature.auth.dto.response.UserResponse;
import org.group12.feature.auth.enums.AuthResponseCode;
import org.group12.feature.auth.service.AuthenticationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;

    // POST /v1/auth/login
    @PostMapping("/login")
    public ApiResponse<AuthenticateResponse> login(
            @Valid @RequestBody LoginRequest req) {

        AuthenticateResponse response = authService.authenticate(req);

        return ApiResponse.<AuthenticateResponse>builder()
                .code(AuthResponseCode.LOGIN_SUCCESS.getCode())
                .message(AuthResponseCode.LOGIN_SUCCESS.getMessage())
                .data(response)
                .build();
    }


    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        UserResponse response = authService.register(userCreateRequest);

        return ApiResponse.<UserResponse>builder()
                .code(AuthResponseCode.REGISTER_SUCCESS.getCode())
                .message(AuthResponseCode.REGISTER_SUCCESS.getMessage())
                .data(response)
                .build();
    }

    // POST /v1/auth/refresh-token
    @PostMapping("/refresh-token")
    public ApiResponse<AuthenticateResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest req) {

        AuthenticateResponse response = authService.refreshToken(req);

        return ApiResponse.<AuthenticateResponse>builder()
                .code(AuthResponseCode.TOKEN_REFRESH_SUCCESS.getCode())
                .message(AuthResponseCode.TOKEN_REFRESH_SUCCESS.getMessage())
                .data(response)
                .build();
    }

    // POST /v1/auth/logout
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Valid @RequestBody LogoutRequest req) {

        authService.logout(req);

        return ApiResponse.<Void>builder()
                .code(AuthResponseCode.LOGOUT_SUCCESS.getCode())
                .message(AuthResponseCode.LOGOUT_SUCCESS.getMessage())
                .build();
    }

    @GetMapping("/test")
    public ApiResponse<String> test() {
        return ApiResponse.<String>builder()
                .code(AuthResponseCode.LOGIN_SUCCESS.getCode())
                .message(AuthResponseCode.LOGIN_SUCCESS.getMessage())
                .data(authService.testAccessDenied())
                .build();
    }
}