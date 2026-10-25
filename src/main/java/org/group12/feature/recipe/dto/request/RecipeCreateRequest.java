package org.group12.feature.recipe.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.recipe.enums.Difficulty;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Request body for creating a custom recipe")
public class RecipeCreateRequest {

    @Schema(description = "Recipe title", example = "Thịt kho tàu")
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Schema(description = "Number of servings", example = "4")
    @NotNull(message = "Servings is required")
    @Positive(message = "Servings must be greater than 0")
    @Max(value = 100, message = "Servings must not exceed 100")
    private Integer servings;

    @Schema(description = "Preparation time in minutes", example = "90")
    @Positive(message = "Ready time must be greater than 0")
    private Integer readyInMinutes;

    @Schema(description = "Difficulty", example = "MEDIUM")
    private Difficulty difficulty;

    @Schema(description = "Cuisine id (see GET /v1/cuisines)")
    private UUID cuisineId;

    @Schema(description = "Ingredient lines")
    @NotEmpty(message = "At least one ingredient is required")
    @Size(max = 100, message = "A recipe can have at most 100 ingredients")
    @Valid
    private List<RecipeIngredientRequest> ingredients;

    @Schema(description = "Cooking steps in order")
    @NotEmpty(message = "At least one step is required")
    @Size(max = 100, message = "A recipe can have at most 100 steps")
    @Valid
    private List<RecipeStepRequest> steps;
}
