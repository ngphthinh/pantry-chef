package org.group12.feature.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private String measurementSystem;
    private String themeMode;
}
