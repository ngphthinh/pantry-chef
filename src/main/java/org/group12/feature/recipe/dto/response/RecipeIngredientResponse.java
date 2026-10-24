package org.group12.feature.recipe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.common.enums.Unit;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RecipeIngredientResponse {
    private UUID id;
    private String name;
    private Double amount;
    private Unit unit;
}
