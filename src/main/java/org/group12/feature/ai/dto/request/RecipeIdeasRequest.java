package org.group12.feature.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Request body for AI recipe ideas")
public class RecipeIdeasRequest {
    @Min(1) @Max(10)
    @Schema(description = "Number of recipe ideas to generate. Must be between 1 and 10.", example = "5")
    private int limit = 5;

    @Min(5) @Max(480)
    @Schema(description = "Maximum cooking time in minutes. Optional. If not provided, the system will choose a suitable time for you.", example = "30")
    private Integer maxCookingMinutes;   // optional

    @Schema(description = "Cuisine type for the recipe ideas. Optional. If not provided, the system will choose a cuisine for you.", example = "Italian")
    private String cuisine;              // optional
}