package org.group12.feature.pantry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.common.enums.Unit;
import org.group12.feature.pantry.enums.StorageLocation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PantryItemResponse {
    private UUID id;
    private UUID ingredientId;
    private String ingredientName;
    private UUID categoryId;
    private String categoryName;
    private Double quantity;
    private Unit unit;
    private StorageLocation location;
    private LocalDate expiryDate;
    private String photoUri;
    private Instant createdAt;
    private Instant updatedAt;
}
