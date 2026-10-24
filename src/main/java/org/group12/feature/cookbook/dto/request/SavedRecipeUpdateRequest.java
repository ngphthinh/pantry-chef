package org.group12.feature.cookbook.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.cookbook.validation.annotation.ValidSavedRecipeUpdateRequest;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ValidSavedRecipeUpdateRequest
public class SavedRecipeUpdateRequest {
    @Min(1)
    @Max(5)
    private Integer rating;
    @Size(max = 2000)
    private String note;
    private Boolean favorite;

}
