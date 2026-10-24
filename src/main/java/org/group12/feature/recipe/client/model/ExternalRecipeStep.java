package org.group12.feature.recipe.client.model;

import java.io.Serializable;

public record ExternalRecipeStep(int order, String text, Integer timerSeconds) implements Serializable {
}
