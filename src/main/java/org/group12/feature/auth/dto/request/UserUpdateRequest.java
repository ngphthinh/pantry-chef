// org.group12.feature.auth.dto.UserUpdateRequest
package org.group12.feature.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.group12.feature.auth.enums.MeasurementSystem;
import org.group12.feature.auth.enums.ThemeMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for updating user information")
public class UserUpdateRequest {
    @Schema(description = "Display name of the user", example = "John Doe")
    @Size(min = 1, max = 100, message = "displayName must be 1-100 characters")
    @Pattern(regexp = ".*\\S.*", message = "displayName must not be blank")
    private String displayName;


    @Schema(description = "Measurement system preference of the user", example = "METRIC")
    private MeasurementSystem measurementSystem;
    @Schema(description = "Theme mode preference of the user", example = "SYSTEM")
    private ThemeMode themeMode;
}