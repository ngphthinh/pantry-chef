package org.group12.feature.ingredient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class IngredientCategoryResponse {
    private UUID id;
    private String code;
    private String name;
}
