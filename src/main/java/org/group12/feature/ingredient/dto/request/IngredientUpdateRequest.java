package org.group12.feature.ingredient.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request DTO for creating a new ingredient")
public class IngredientUpdateRequest {
    @Schema(description = "Name of the ingredient", example = "Sugar")
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @Schema(description = "Default unit of the ingredient", example = "G")
    private Unit defaultUnit;

    @Schema(description = "Category ID of the ingredient", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "Category is required")
    private UUID categoryId;

    @Schema(description = "Barcode of the ingredient", example = "1234567890123")
    @Pattern(regexp = "^\\d{8,14}$", message = "Barcode must be 8 to 14 digits")
    private String barcode;
}
