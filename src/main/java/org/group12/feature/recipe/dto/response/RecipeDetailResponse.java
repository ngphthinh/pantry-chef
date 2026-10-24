package org.group12.feature.recipe.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema
public class RecipeDetailResponse {
    private UUID id;
    private String title;
    private String imageUrl;
    private String sourceUrl;
    private String externalId;
    private Integer servings;
    private Integer readyInMinutes;
    private String difficulty;
    private boolean custom;
    private CuisineResponse cuisine;
}