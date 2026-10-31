package org.group12.feature.recipe.client.model;

import java.io.Serializable;
import java.util.List;

/**
 * Một dòng kết quả tìm kiếm. {@code usedIngredientCount}/{@code missedIngredientCount} chỉ có giá trị
 * khi tìm theo nguyên liệu, các trường hợp khác là {@code null}.
 */
public record ExternalRecipeSummary(
        String externalId,
        String title,
        String imageUrl,
        Integer readyInMinutes,
        Integer servings,
        String cuisineName,
        Integer usedIngredientCount,
        Integer missedIngredientCount,
        List<String> missedIngredients
) implements Serializable {

    public ExternalRecipeSummary {
        missedIngredients = missedIngredients == null ? List.of() : List.copyOf(missedIngredients);
    }
}
