package org.group12.feature.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.auth.enums.MeasurementSystem;
import org.group12.feature.auth.enums.ThemeMode;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserResponse {
    private UUID id;
    private String email;
    private String displayName;
    private String avatarUri;
    private MeasurementSystem measurementSystem;
    private ThemeMode themeMode;
}
