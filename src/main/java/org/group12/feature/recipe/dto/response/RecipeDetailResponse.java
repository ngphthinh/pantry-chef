package org.group12.feature.recipe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.group12.feature.recipe.enums.Difficulty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Chi tiết đầy đủ (nguyên liệu, các bước, ảnh) để mobile cache xem offline. */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RecipeDetailResponse {
    private UUID id;
    private String externalId;
    private String title;
    private String imageUrl;
    private Integer servings;
    private Integer readyInMinutes;
    private Difficulty difficulty;
    private String sourceUrl;
    private boolean custom;
    private CuisineResponse cuisine;
    private List<RecipeIngredientResponse> ingredients;
    private List<RecipeStepResponse> steps;
    private Instant createdAt;
    private Instant updatedAt;
}
