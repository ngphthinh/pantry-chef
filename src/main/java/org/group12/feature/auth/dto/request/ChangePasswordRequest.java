package org.group12.feature.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.auth.validation.annotation.ValidPassword;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Request object for changing password")
public class ChangePasswordRequest {
    @Schema(description = "Old password of the user", example = "oldPassword123!")
    @ValidPassword(message = "Old password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    @NotBlank(message = "Old password must not be blank")
    private String oldPassword;
    @NotBlank(message = "New password must not be blank")
    @ValidPassword(message = "New password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    @Schema(description = "New password for the user", example = "newSecurePassword123!")
    private String newPassword;
}
