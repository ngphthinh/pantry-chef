package org.group12.feature.cookbook.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record SavedRecipeUpdateRequest(
        @Min(1) @Max(5) Integer rating,
        @Size(max = 2000) String note,
        Boolean favorite
) {
    @AssertTrue(message = "Phải truyền ít nhất một trong: rating, note, favorite")
    public boolean isAtLeastOneField() {
        return rating != null || note != null || favorite != null;
    }
}
