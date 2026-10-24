package org.group12.feature.recipe.client.model;

import org.group12.common.enums.Unit;

import java.io.Serializable;

public record ExternalRecipeIngredient(String name, Double amount, Unit unit) implements Serializable {
}
