package org.group12.feature.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.feature.auth.dto.request.*;
import org.group12.feature.auth.dto.response.AuthenticateResponse;
import org.group12.feature.auth.dto.response.IntrospectResponse;
import org.group12.feature.auth.dto.response.UserResponse;
import org.group12.feature.auth.enums.AuthResponseCode;
import org.group12.feature.auth.service.AuthenticationService;
import org.springframework.http.HttpStatus;
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

    // no content 204
    @PatchMapping("/change-password")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        authService.changePassword(changePasswordRequest);
    }

    @PatchMapping("/reset-password")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        authService.resetPassword(resetPasswordRequest);
    }


    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@Valid @RequestBody IntrospectRequest introspectRequest) {
        IntrospectResponse response = authService.introspect(introspectRequest);

        return ApiResponse.<IntrospectResponse>builder()
                .code(AuthResponseCode.INTROSPECT_SUCCESS.getCode())
                .message(AuthResponseCode.INTROSPECT_SUCCESS.getMessage())
                .data(response)
                .build();
    }

    @PatchMapping("/forgot-password")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authService.forgotPassword(forgotPasswordRequest);
    }

    @GetMapping("/test")
    public String test() {
        return "Test";
    }

}