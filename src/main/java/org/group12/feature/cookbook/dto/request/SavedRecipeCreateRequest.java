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

    private UUID externalId;

    private UUID recipeId;
}
