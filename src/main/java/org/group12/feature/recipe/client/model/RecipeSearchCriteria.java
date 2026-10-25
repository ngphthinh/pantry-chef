package org.group12.feature.recipe.client.model;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSearchCriteria implements Serializable {

    private List<String> includeIngredients;
    private List<String> excludeIngredients;
    private String query;
    private String cuisineName;
    private Integer maxReadyMinutes;
    private RecipeSort sort;

    public void setIncludeIngredients(List<String> includeIngredients) {
        this.includeIngredients = includeIngredients == null
                ? List.of()
                : List.copyOf(includeIngredients);
    }

    public void setExcludeIngredients(List<String> excludeIngredients) {
        this.excludeIngredients = excludeIngredients == null
                ? List.of()
                : List.copyOf(excludeIngredients);
    }

    public void setSort(RecipeSort sort) {
        this.sort = sort == null ? RecipeSort.RELEVANCE : sort;
    }
}