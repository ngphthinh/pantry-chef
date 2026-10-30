package org.group12.feature.ingredient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.common.enums.Unit;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class IngredientResponse {
    private UUID id;
    private String name;
    private Unit defaultUnit;
    private String barcode;
    private IngredientCategoryResponse category;
    private boolean system;
}
