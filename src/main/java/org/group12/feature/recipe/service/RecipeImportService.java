package org.group12.feature.recipe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.utils.AppUtil;
import org.group12.feature.recipe.client.model.ExternalRecipeDetail;
import org.group12.feature.recipe.client.model.ExternalRecipeSummary;
import org.group12.feature.recipe.dto.response.RecipeDetailResponse;
import org.group12.feature.recipe.entity.Cuisine;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.entity.RecipeIngredient;
import org.group12.feature.recipe.entity.RecipeStep;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.mapper.RecipeMapper;
import org.group12.feature.recipe.repository.CuisineRepository;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Lưu bản sao cục bộ của công thức từ API ngoài. Cần thiết vì {@code SavedRecipe} và {@code MealPlanEntry}
 * trỏ tới {@code Recipe.id}. Tách thành bean riêng và không dùng transaction ở {@link RecipeService}
 * để không giữ kết nối DB trong lúc chờ API ngoài.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeImportService {

    private static final int MAX_TITLE_LENGTH = 255;

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
                .externalId(detail.externalId())
                .title(detail.title())
                .imageUrl(detail.imageUrl())
                .servings(detail.servings())
                .readyInMinutes(detail.readyInMinutes())
                .difficulty(Difficulty.fromReadyInMinutes(detail.readyInMinutes()))
                .sourceUrl(detail.sourceUrl())
                .custom(false)
                .cuisine(findCuisine(detail.cuisineName()))
                .build();

        addIngredientsAndSteps(recipe, detail);

        Recipe saved = recipeRepository.saveAndFlush(recipe);
        log.info("Imported external recipe {} as {}", saved.getExternalId(), saved.getId());
        return recipeMapper.toDetailResponse(saved);
    }


    @Transactional(readOnly = true)
    public Map<String, UUID> findIds(Collection<String> externalIds) {
        return recipeRepository.findIdsByExternalIdIn(externalIds).stream()
                .collect(Collectors.toMap(RecipeRepository.ExternalIdRow::getExternalId,
                        RecipeRepository.ExternalIdRow::getId, (a, b) -> a));
    }

    /**
     * Lưu nhanh một dòng kết quả tìm kiếm (chưa có nguyên liệu và các bước). Phần chi tiết được bổ sung bởi
     * {@link #completeRecipe} khi người dùng mở chi tiết công thức.
     */
    @Transactional
    public UUID importSummary(ExternalRecipeSummary summary, Cuisine cuisine) {
        String title = summary.title().trim();
        Recipe recipe = Recipe.builder()
                .id(appUtil.generateUUID())
                .externalId(summary.externalId())
                .title(title.length() > MAX_TITLE_LENGTH ? title.substring(0, MAX_TITLE_LENGTH) : title)
                .imageUrl(summary.imageUrl())
                .servings(summary.servings())
                .readyInMinutes(summary.readyInMinutes())
                .difficulty(Difficulty.fromReadyInMinutes(summary.readyInMinutes()))
                .custom(false)
                .cuisine(cuisine)
                .build();
        return recipeRepository.saveAndFlush(recipe).getId();
    }

    /**
     * Bổ sung nguyên liệu và các bước cho công thức đã lưu từ kết quả tìm kiếm. Không làm gì nếu đã có chi tiết.
     */
    @Transactional
    public RecipeDetailResponse completeRecipe(ExternalRecipeDetail detail) {
        Recipe recipe = recipeRepository.findForUpdateByExternalId(detail.externalId())
                .orElseThrow(RecipeNotFoundException::new);

        if (recipe.getIngredients().isEmpty() && recipe.getSteps().isEmpty()) {
            recipe.setSourceUrl(detail.sourceUrl());
            if (recipe.getImageUrl() == null) {
                recipe.setImageUrl(detail.imageUrl());
            }
            if (recipe.getServings() == null) {
                recipe.setServings(detail.servings());
            }
            if (recipe.getReadyInMinutes() == null) {
                recipe.setReadyInMinutes(detail.readyInMinutes());
                recipe.setDifficulty(Difficulty.fromReadyInMinutes(detail.readyInMinutes()));
            }
            if (recipe.getCuisine() == null) {
                recipe.setCuisine(findCuisine(detail.cuisineName()));
            }
            addIngredientsAndSteps(recipe, detail);
            recipeRepository.saveAndFlush(recipe);
        }
        return recipeMapper.toDetailResponse(recipe);
    }

    private void addIngredientsAndSteps(Recipe recipe, ExternalRecipeDetail detail) {
        detail.ingredients().forEach(ingredient -> recipe.getIngredients().add(RecipeIngredient.builder()
                .id(appUtil.generateUUID())
                .recipe(recipe)
                .name(ingredient.name())
                .amount(ingredient.amount())
                .unit(ingredient.unit())
                .build()));
        detail.steps().forEach(step -> recipe.getSteps().add(RecipeStep.builder()
                .id(appUtil.generateUUID())
                .recipe(recipe)
                .stepOrder(step.order())
                .text(step.text())
                .timerSeconds(step.timerSeconds())
                .build()));
    }

    private Cuisine findCuisine(String cuisineName) {
        if (cuisineName == null || cuisineName.isBlank()) {
            return null;
        }
        return cuisineRepository.findAllByOrderByNameAsc().stream()
                .filter(cuisine -> cuisine.getName().equalsIgnoreCase(cuisineName.trim()))
                .findFirst()
                .orElse(null);
    }
}
