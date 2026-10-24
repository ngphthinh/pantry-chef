package org.group12.feature.pantry.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.common.enums.Unit;
import org.group12.feature.pantry.enums.StorageLocation;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PantryItemCreateRequest {
    @NotNull(message = "Ingredient is required")
    private UUID ingredientId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Double quantity;

    @NotNull(message = "Unit is required")
    private Unit unit;

    private StorageLocation location;
    private LocalDate expiryDate;
    private String photoUri;
}
