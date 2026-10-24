package org.group12.feature.recipe.client.model;

import java.io.Serializable;
import java.util.List;

/** Một trang kết quả thô từ nhà cung cấp (chưa lọc), kèm tổng số kết quả để tính {@code hasNext}. */
public record ExternalRecipePage(List<ExternalRecipeSummary> items, int totalResults) implements Serializable {

    public ExternalRecipePage {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
