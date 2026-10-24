package org.group12.feature.recipe.dto.request;

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
@Schema(description = "One ingredient line of a custom recipe")
public class RecipeIngredientRequest {

    @Schema(description = "Ingredient name", example = "Thịt ba chỉ")
    @NotBlank(message = "Ingredient name is required")
    @Size(max = 255, message = "Ingredient name must not exceed 255 characters")
    private String name;

    @Schema(description = "Amount needed, omit for 'to taste'", example = "500")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @Schema(description = "Unit of the amount", example = "G")
    private Unit unit;
}
