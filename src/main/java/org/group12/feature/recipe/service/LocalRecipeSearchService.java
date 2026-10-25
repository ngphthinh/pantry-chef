package org.group12.feature.recipe.service;

import lombok.RequiredArgsConstructor;
import org.group12.feature.recipe.dto.projection.MissingRow;
import org.group12.feature.recipe.dto.projection.RecipeMatchRow;
import org.group12.feature.recipe.dto.response.RecipeSummaryResponse;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.recipe.mapper.RecipeMapper;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.group12.feature.recipe.repository.RecipeRepository.LocalIngredientRow;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        List<String> includeNames = normalize(include);
        if (includeNames.isEmpty()) {
            return List.of();
        }
        List<String> excludedNames = normalize(excluded);
        if (excludedNames.isEmpty()) {
            excludedNames = List.of("");          // tránh "in ()" rỗng
        }

        List<RecipeMatchRow> rows = recipeRepository.findIngredientMatches(
                userId, includeNames, excludedNames, cuisineId, maxMinutes, difficulty,
                PageRequest.of(0, MAX_LOCAL_RESULTS));
        if (rows.isEmpty()) {
            return List.of();
        }

        List<UUID> ids = rows.stream().map(RecipeMatchRow::getRecipeId).toList();

        Map<UUID, List<String>> missingByRecipe = recipeRepository
                .findMissingNames(ids, includeNames).stream()
                .collect(Collectors.groupingBy(MissingRow::getRecipeId,
                        Collectors.mapping(MissingRow::getName, Collectors.toList())));

        Map<UUID, Recipe> recipes = recipeRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity()));

        // giữ nguyên thứ tự DB đã sắp xếp
        return rows.stream()
                .filter(row -> recipes.containsKey(row.getRecipeId()))
                .map(row -> {
                    List<String> missing = missingByRecipe.getOrDefault(row.getRecipeId(), List.of());
                    int used = (int) row.getUsed();
                    int total = (int) row.getTotal();

                    RecipeSummaryResponse response =
                            recipeMapper.toSummaryResponse(recipes.get(row.getRecipeId()));
                    response.setUsedIngredientCount(used);
                    response.setMissedIngredientCount(missing.size());
                    response.setMatchScore(total == 0 ? 0.0 : (double) used / total);
                    response.setMissingIngredients(missing);
                    return response;
                })
                .toList();
    }

    private List<String> normalize(Collection<String> names) {
        if (names == null) return List.of();
        return names.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }
    // ---------- private helpers ----------


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
