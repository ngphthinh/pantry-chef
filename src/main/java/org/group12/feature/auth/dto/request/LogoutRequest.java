package org.group12.feature.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LogoutRequest {
    @NotBlank(message = "Access token is required")
    private String accessToken;
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
