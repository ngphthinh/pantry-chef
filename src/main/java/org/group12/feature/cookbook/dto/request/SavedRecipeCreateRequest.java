package org.group12.feature.cookbook.dto.request;


import java.util.UUID;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.cookbook.validation.annotation.ValidSavedRecipeCreateRequest;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidSavedRecipeCreateRequest
public class SavedRecipeCreateRequest {

    /** Id công thức bên API ngoài (chuỗi, ví dụ "716429"). */
    private String externalId;

    private UUID recipeId;
}
