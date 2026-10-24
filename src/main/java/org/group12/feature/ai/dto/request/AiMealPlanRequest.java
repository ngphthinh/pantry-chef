package org.group12.feature.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for AI meal plan draft")
public class AiMealPlanRequest {

    @NotNull
    @Schema(description = "The start date of the week for the meal plan.")
    private Instant weekStart;

    @Schema(description = "Number of days for the meal plan. Must be between 1 and 7.", example = "7")
    @Min(1)
    @Max(7)
    private int days;

    @Schema(description = "UUID of the cuisine. Optional. If not provided, the system will choose a cuisine for you.")
    private UUID cuisineId;          // optional

    @Schema(description = "Maximum number of minutes for cooking. Optional. If not provided, the system will choose a suitable time for you.")
    @Min(5)
    @Max(480)
    private Integer maxMinutes;      // optional
}