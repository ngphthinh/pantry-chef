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
@Schema
public class ShoppingItemUpdateRequest {
    @Pattern(regexp = ".*\\S.*", message = "Name must not be blank")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Positive(message = "Quantity must be greater than 0")
    private int quantity;

    private Unit unit;

    private ShoppingItemStatus status;
}
