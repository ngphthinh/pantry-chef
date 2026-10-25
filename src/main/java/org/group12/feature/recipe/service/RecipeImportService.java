package org.group12.feature.recipe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.utils.AppUtil;
import org.group12.feature.recipe.client.model.ExternalRecipeDetail;
import org.group12.feature.recipe.dto.response.RecipeDetailResponse;
import org.group12.feature.recipe.entity.Cuisine;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.entity.RecipeIngredient;
import org.group12.feature.recipe.entity.RecipeStep;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.recipe.mapper.RecipeMapper;
import org.group12.feature.recipe.repository.CuisineRepository;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Lưu bản sao cục bộ của công thức từ API ngoài. Cần thiết vì {@code SavedRecipe} và {@code MealPlanEntry}
 * trỏ tới {@code Recipe.id}. Tách thành bean riêng và không dùng transaction ở {@link RecipeService}
 * để không giữ kết nối DB trong lúc chờ API ngoài.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeImportService {

    private final RecipeRepository recipeRepository;
    private final CuisineRepository cuisineRepository;
    private final RecipeMapper recipeMapper;
    private final AppUtil appUtil;

    @Transactional(readOnly = true)
    public Optional<RecipeDetailResponse> findImported(String externalId) {
        return recipeRepository.findDetailByExternalId(externalId).map(recipeMapper::toDetailResponse);
    }

    /**
     * @throws org.springframework.dao.DataIntegrityViolationException nếu một request khác vừa lưu cùng
     *                                                                 {@code externalId} (ràng buộc unique)
     */
    @Transactional
    public RecipeDetailResponse importRecipe(ExternalRecipeDetail detail) {
        Recipe recipe = Recipe.builder()
                .id(appUtil.generateUUID())
                .externalId(detail.getExternalId())
                .title(detail.getTitle())
                .imageUrl(detail.getImageUrl())
                .servings(detail.getServings())
                .readyInMinutes(detail.getReadyInMinutes())
                .difficulty(Difficulty.fromReadyInMinutes(detail.getReadyInMinutes()))
                .sourceUrl(detail.getSourceUrl())
                .custom(false)
                .cuisine(findCuisine(detail.getCuisineName()))
                .build();

        detail.getIngredients().forEach(ingredient -> recipe.getIngredients().add(RecipeIngredient.builder()
                .id(appUtil.generateUUID())
                .recipe(recipe)
                .name(ingredient.getName())
                .amount(ingredient.getAmount())
                .unit(ingredient.getUnit())
                .build()));
        detail.getSteps().forEach(step -> recipe.getSteps().add(RecipeStep.builder()
                .id(appUtil.generateUUID())
                .recipe(recipe)
                .stepOrder(step.getOrder())
                .text(step.getText())
                .timerSeconds(step.getTimerSeconds())
                .build()));

        Recipe saved = recipeRepository.saveAndFlush(recipe);
        log.info("Imported external recipe {} as {}", saved.getExternalId(), saved.getId());
        return recipeMapper.toDetailResponse(saved);
    }

    private Cuisine findCuisine(String cuisineName) {
        if (cuisineName == null || cuisineName.isBlank()) {
            return null;
        }
        return cuisineRepository.findByNameIgnoreCase((cuisineName.trim()))
                .orElse(null);

    }
}
