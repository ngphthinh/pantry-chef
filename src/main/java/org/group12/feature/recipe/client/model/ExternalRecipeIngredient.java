package org.group12.feature.recipe.client.model;

import lombok.*;
import org.group12.common.enums.Unit;

import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalRecipeIngredient implements Serializable {

    private String name;
    private Double amount;
    private Unit unit;
}