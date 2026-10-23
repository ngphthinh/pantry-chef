package org.group12.feature.shopping.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
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
@Schema(description = "Request body for updating a shopping item")
public class ShoppingItemUpdateRequest {
    @Schema(description = "Name of the shopping item", example = "Milk")
    @Pattern(regexp = ".*\\S.*", message = "Name must not be blank")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Schema(description = "Quantity of the shopping item", example = "2")
    @Positive(message = "Quantity must be greater than 0")
    private int quantity;

    @Schema(description = "Unit of the shopping item", example = "LITER")
    private Unit unit;

    @Schema(description = "Status of the shopping item", example = "PENDING")
    private ShoppingItemStatus status;
}
