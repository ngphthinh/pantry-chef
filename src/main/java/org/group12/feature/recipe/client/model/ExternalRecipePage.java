package org.group12.feature.recipe.client.model;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalRecipePage implements Serializable {

    private List<ExternalRecipeSummary> items;
    private int totalResults;

    public void setItems(List<ExternalRecipeSummary> items) {
        this.items = items == null ? List.of() : List.copyOf(items);
    }
}