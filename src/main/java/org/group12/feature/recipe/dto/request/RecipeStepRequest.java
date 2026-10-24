package org.group12.feature.recipe.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "One cooking step of a custom recipe. The order is the position in the list")
public class RecipeStepRequest {

    @Schema(description = "Instruction text", example = "Ướp thịt với nước mắm và đường trong 15 phút")
    @NotBlank(message = "Step text is required")
    @Size(max = 5000, message = "Step text must not exceed 5000 characters")
    private String text;

    @Schema(description = "Optional timer for this step, in seconds", example = "900")
    @Positive(message = "Timer must be greater than 0")
    private Integer timerSeconds;
}
