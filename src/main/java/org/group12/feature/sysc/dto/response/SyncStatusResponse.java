package org.group12.feature.sysc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SyncStatusResponse {
    private UUID id;
    private String code;
    private String name;
}
