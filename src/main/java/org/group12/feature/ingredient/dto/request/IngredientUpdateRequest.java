package org.group12.feature.ingredient.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.common.enums.Unit;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class IngredientUpdateRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    private Unit defaultUnit;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    @Pattern(regexp = "^\\d{8,14}$", message = "Barcode must be 8 to 14 digits")
    private String barcode;
}
