package org.group12.feature.cookbook.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectionCreateRequest(
        @NotBlank @Size(max = 100) String name
) {
}
