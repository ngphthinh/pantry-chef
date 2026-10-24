package org.group12.feature.shopping.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.common.enums.Unit;
import org.group12.feature.shopping.enums.ShoppingItemStatus;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Request body for updating a shopping item. All fields are required; if any field is missing or null the update is rejected")
public class ShoppingItemUpdateRequest {

    @Schema(description = "Name of the shopping item", example = "Milk")
    @NotBlank(message = "Name must not be blank")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Schema(description = "Quantity of the shopping item", example = "2")
    @NotNull(message = "Quantity must not be null")
    @Positive(message = "Quantity must be greater than 0")
    private int quantity;

    @Schema(description = "Unit of the shopping item", example = "L")
    @NotNull(message = "Unit must not be null")
    private Unit unit;

    @Schema(description = "Status of the shopping item", example = "PENDING")
    @NotNull(message = "Status must not be null")
    private ShoppingItemStatus status;
}