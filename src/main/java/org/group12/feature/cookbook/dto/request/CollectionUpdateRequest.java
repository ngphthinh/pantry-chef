package org.group12.feature.cookbook.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectionUpdateRequest(
        @NotBlank @Size(max = 100) String name
) {
}
