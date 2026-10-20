package org.group12.feature.shopping.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShoppingListDetailResponse {
    private UUID id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    @Builder.Default
    private List<ShoppingItemResponse> items = new ArrayList<>();
}
