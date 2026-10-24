package org.group12.feature.recipe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RecipeStepResponse {
    private UUID id;
    private int stepOrder;
    private String text;
    private Integer timerSeconds;
}
