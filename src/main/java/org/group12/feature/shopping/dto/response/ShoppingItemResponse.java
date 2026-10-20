package org.group12.feature.shopping.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.common.enums.Unit;
import org.group12.feature.shopping.enums.ShoppingItemStatus;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShoppingItemResponse {
    private UUID id;
    private String name;
    private Double quantity;
    private Unit unit;
    private ShoppingItemStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
