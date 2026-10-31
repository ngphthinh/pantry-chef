package org.group12.feature.recipe.client.model;

import java.io.Serializable;
import java.util.List;

/**
 * Tiêu chí tìm kiếm không phụ thuộc nhà cung cấp. Dùng làm một phần của khóa cache nên
 * phải sắp xếp/chuẩn hóa danh sách trước khi tạo.
 */
public record RecipeSearchCriteria(
        List<String> includeIngredients,
        List<String> excludeIngredients,
        String query,
        String cuisineName,
        Integer maxReadyMinutes,
        RecipeSort sort
) implements Serializable {

    public RecipeSearchCriteria {
        includeIngredients = includeIngredients == null ? List.of() : List.copyOf(includeIngredients);
        excludeIngredients = excludeIngredients == null ? List.of() : List.copyOf(excludeIngredients);
        sort = sort == null ? RecipeSort.RELEVANCE : sort;
    }
}
