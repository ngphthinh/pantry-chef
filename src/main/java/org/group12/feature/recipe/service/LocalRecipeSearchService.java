package org.group12.feature.recipe.service;

import lombok.RequiredArgsConstructor;
import org.group12.feature.recipe.dto.response.RecipeSummaryResponse;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.recipe.mapper.RecipeMapper;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.group12.feature.recipe.repository.RecipeRepository.LocalIngredientRow;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tìm trong công thức đang có trong DB: công thức tự tạo của chính người dùng và các công thức không phải tự tạo
 * (dữ liệu seed, công thức từ API ngoài đã được lưu về). Kết quả được {@link RecipeService} ghép lên trước
 * kết quả từ API ngoài.
 * <p>
 * Khớp theo chuỗi con, không phân biệt hoa thường nhưng vẫn phân biệt dấu ("mì" không khớp "mi").
 * Phần chấm điểm theo nguyên liệu chạy trong bộ nhớ trên toàn bộ nguyên liệu người dùng được xem; với vài trăm
 * công thức thì ổn, khi dữ liệu lớn lên cần chuyển sang truy vấn có phân trang.
 */
@Service
@RequiredArgsConstructor
public class LocalRecipeSearchService {

    static final int MAX_LOCAL_RESULTS = 10;
    private static final char LIKE_ESCAPE = '\\';

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    @Transactional(readOnly = true)
    public List<RecipeSummaryResponse> searchByName(UUID userId, String keyword, Collection<String> excluded) {
        String normalized = normalize(keyword);
        if (normalized.isEmpty()) {
            return List.of();
        }

        List<Recipe> recipes = recipeRepository.findAccessibleByTitle(
                userId, "%" + escapeLike(normalized) + "%", PageRequest.of(0, MAX_LOCAL_RESULTS));
        if (recipes.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<String>> ingredientsByRecipe = groupByRecipe(recipeRepository.findIngredientRowsByRecipeIds(
                recipes.stream().map(Recipe::getId).toList()));
        return recipes.stream()
                .filter(recipe -> !containsExcluded(ingredientsByRecipe.getOrDefault(recipe.getId(), List.of()), excluded))
                .map(recipeMapper::toSummaryResponse)
                .toList();
    }

    /**
     * Chấm điểm theo số nguyên liệu khớp: món dùng được nhiều nguyên liệu nhất và thiếu ít nhất lên trước.
     * Một nguyên liệu của công thức được coi là khớp khi tên của nó chứa tên nguyên liệu đang tìm.
     */
    @Transactional(readOnly = true)
    public List<RecipeSummaryResponse> searchByIngredients(
            UUID userId, Collection<String> include, Collection<String> excluded,
            UUID cuisineId, Integer maxMinutes, Difficulty difficulty) {
        if (include.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<String>> ingredientsByRecipe =
                groupByRecipe(recipeRepository.findAccessibleIngredientRows(userId));

        Map<UUID, Match> matches = new HashMap<>();
        ingredientsByRecipe.forEach((recipeId, ingredients) -> {
            if (containsExcluded(ingredients, excluded)) {
                return;
            }
            Match match = score(ingredients, include);
            if (match.used() > 0) {
                matches.put(recipeId, match);
            }
        });
        if (matches.isEmpty()) {
            return List.of();
        }

        List<RecipeSummaryResponse> results = new ArrayList<>();
        recipeRepository.findByIdIn(matches.keySet()).stream()
                .filter(recipe -> matchesFilters(recipe, cuisineId, maxMinutes, difficulty))
                .sorted(Comparator
                        .comparingInt((Recipe recipe) -> -matches.get(recipe.getId()).used())
                        .thenComparingInt(recipe -> matches.get(recipe.getId()).missing().size())
                        .thenComparing(Recipe::getTitle, String.CASE_INSENSITIVE_ORDER))
                .limit(MAX_LOCAL_RESULTS)
                .forEach(recipe -> {
                    Match match = matches.get(recipe.getId());
                    RecipeSummaryResponse response = recipeMapper.toSummaryResponse(recipe);
                    response.setUsedIngredientCount(match.used());
                    response.setMissedIngredientCount(match.missing().size());
                    response.setMatchScore((double) match.used() / (match.used() + match.missing().size()));
                    response.setMissingIngredients(match.missing());
                    results.add(response);
                });
        return results;
    }

    // ---------- private helpers ----------

    private record Match(int used, List<String> missing) {
    }

    private Match score(List<String> recipeIngredients, Collection<String> include) {
        Set<String> usedQueries = new HashSet<>();
        List<String> missing = new ArrayList<>();
        for (String ingredient : recipeIngredients) {
            String normalized = normalize(ingredient);
            String hit = include.stream().filter(normalized::contains).findFirst().orElse(null);
            if (hit == null) {
                missing.add(ingredient.trim());
            } else {
                usedQueries.add(hit);
            }
        }
        return new Match(usedQueries.size(), missing);
    }

    private boolean matchesFilters(Recipe recipe, UUID cuisineId, Integer maxMinutes, Difficulty difficulty) {
        if (cuisineId != null && (recipe.getCuisine() == null || !cuisineId.equals(recipe.getCuisine().getId()))) {
            return false;
        }
        if (maxMinutes != null && (recipe.getReadyInMinutes() == null || recipe.getReadyInMinutes() > maxMinutes)) {
            return false;
        }
        return difficulty == null || difficulty == recipe.getDifficulty();
    }

    private boolean containsExcluded(List<String> recipeIngredients, Collection<String> excluded) {
        return recipeIngredients.stream()
                .map(this::normalize)
                .anyMatch(name -> excluded.stream().anyMatch(name::contains));
    }

    private Map<UUID, List<String>> groupByRecipe(List<LocalIngredientRow> rows) {
        Map<UUID, List<String>> grouped = new LinkedHashMap<>();
        for (LocalIngredientRow row : rows) {
            if (row.getName() != null && !row.getName().isBlank()) {
                grouped.computeIfAbsent(row.getRecipeId(), id -> new ArrayList<>()).add(row.getName());
            }
        }
        return grouped;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String escapeLike(String value) {
        return value.replace(String.valueOf(LIKE_ESCAPE), LIKE_ESCAPE + String.valueOf(LIKE_ESCAPE))
                .replace("%", LIKE_ESCAPE + "%")
                .replace("_", LIKE_ESCAPE + "_");
    }
}
