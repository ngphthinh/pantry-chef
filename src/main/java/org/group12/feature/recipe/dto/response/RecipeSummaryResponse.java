package org.group12.feature.recipe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.recipe.enums.Difficulty;

import java.util.List;
import java.util.UUID;

/**
 * Dòng danh sách công thức. {@code id} chỉ có với công thức tự tạo hoặc công thức từ API ngoài đã được
 * lưu về; với kết quả tìm kiếm từ API ngoài dùng {@code externalId} rồi gọi chi tiết để lấy {@code id}.
 * Nhóm trường match chỉ có khi tìm theo nguyên liệu.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RecipeSummaryResponse {
    private UUID id;
    private String externalId;
    private String title;
    private String imageUrl;
    private Integer servings;
    private Integer readyInMinutes;
    private Difficulty difficulty;
    private boolean custom;
    private CuisineResponse cuisine;

    private Integer usedIngredientCount;
    private Integer missedIngredientCount;
    /** Tỉ lệ khớp từ 0 đến 1: used / (used + missed). */
    private Double matchScore;
    private List<String> missingIngredients;
}
