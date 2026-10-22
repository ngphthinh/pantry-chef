package org.group12.feature.pantry.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PantryItemBatchCreateRequest {
    @NotEmpty(message = "Items list cannot be empty")
    private List<@Valid PantryItemCreateRequest> items;
}
