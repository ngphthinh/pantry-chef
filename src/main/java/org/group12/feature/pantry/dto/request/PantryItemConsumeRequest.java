package org.group12.feature.pantry.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PantryItemConsumeRequest {
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Double quantity;
}
