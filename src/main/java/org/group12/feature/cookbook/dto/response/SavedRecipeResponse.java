package org.group12.feature.cookbook.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SavedRecipeResponse{
        private UUID id;
        private UUID recipeId;
        private String recipeTitle;
        private String recipeImageUrl;
        private Integer rating;
        private String note;
        private boolean favorite;
}
