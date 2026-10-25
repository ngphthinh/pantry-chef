package org.group12.feature.recipe.client.model;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalRecipeSummary implements Serializable {

    private String externalId;
    private String title;
    private String imageUrl;
    private Integer readyInMinutes;
    private Integer servings;
    private String cuisineName;
    private Integer usedIngredientCount;
    private Integer missedIngredientCount;
    private List<String> missedIngredients;

    public void setMissedIngredients(List<String> missedIngredients) {
        this.missedIngredients = missedIngredients == null
                ? List.of()
                : List.copyOf(missedIngredients);
    }
}