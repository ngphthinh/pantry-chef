package org.group12.feature.shopping.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.common.enums.Unit;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema
public class ShoppingItemCreateRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Positive(message = "Quantity must be greater than 0")
    private Double quantity;

    private Unit unit;
}
