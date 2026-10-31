package org.group12.feature.recipe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.dto.response.CursorResponse;
import org.group12.common.utils.AppUtil;
import org.group12.feature.ingredient.entity.ExcludedIngredient;
import org.group12.feature.ingredient.repository.ExcludedIngredientRepository;
import org.group12.feature.pantry.entity.PantryItem;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.group12.feature.recipe.client.RecipeProviderClient;
import org.group12.feature.recipe.client.model.ExternalRecipeDetail;
import org.group12.feature.recipe.client.model.ExternalRecipePage;
import org.group12.feature.recipe.client.model.ExternalRecipeSummary;
import org.group12.feature.recipe.client.model.RecipeSearchCriteria;
import org.group12.feature.recipe.client.model.RecipeSort;
import org.group12.feature.recipe.dto.response.RecipeDetailResponse;
import org.group12.feature.recipe.dto.response.RecipeIngredientResponse;
import org.group12.feature.recipe.dto.response.RecipeSummaryResponse;
import org.group12.feature.recipe.entity.Cuisine;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.recipe.exception.CuisineNotFoundException;
import org.group12.feature.recipe.exception.InvalidRecipeCursorException;
import org.group12.feature.recipe.exception.InvalidRecipeSearchException;
import org.group12.feature.recipe.exception.InvalidServingsException;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.exception.RecipeProviderException;
import org.group12.feature.recipe.exception.RecipeProviderQuotaExceededException;
import org.group12.feature.recipe.mapper.CuisineMapper;
import org.group12.feature.recipe.repository.CuisineRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Tìm và xem công thức từ API bên ngoài. Cố ý không đặt {@code @Transactional} ở lớp này: phần lớn thời gian
 * là chờ API ngoài, các thao tác DB đều ngắn và nằm trong transaction riêng của repository/{@link RecipeImportService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class RecipeService {

    private static final int MAX_PAGE_SIZE = 50;
    /** Spoonacular không nhận offset lớn hơn mức này. */
    private static final int MAX_OFFSET = 900;
    private static final int MAX_SEARCH_INGREDIENTS = 10;
    private static final int SIMILAR_COUNT = 10;
    private static final int MAX_RANDOM_COUNT = 10;
    private static final int MAX_SERVINGS = 100;
    private static final double AMOUNT_ROUNDING = 100.0;
    private static final Pattern EXTERNAL_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    private final RecipeProviderClient recipeProviderClient;
    private final RecipeImportService recipeImportService;
    private final LocalRecipeSearchService localRecipeSearchService;
    private final CuisineRepository cuisineRepository;
    private final CuisineMapper cuisineMapper;
    private final PantryItemRepository pantryItemRepository;
    private final ExcludedIngredientRepository excludedIngredientRepository;
    private final AppUtil appUtil;

    // ---------- tìm kiếm ----------

    public CursorResponse<RecipeSummaryResponse> searchByIngredients(
            List<String> ingredients, String cursor, int size, UUID cuisineId, Integer maxMinutes, Difficulty difficulty) {
        UUID userId = appUtil.userIdFromAuthentication();
        List<String> include = normalizeNames(ingredients);
        if (include.isEmpty() || (maxMinutes != null && maxMinutes <= 0)) {
            throw new InvalidRecipeSearchException();
        }

        Cuisine cuisine = cuisineId == null ? null
                : cuisineRepository.findById(cuisineId).orElseThrow(CuisineNotFoundException::new);
        List<String> excluded = excludedNames(userId);
        Integer maxReadyMinutes = effectiveMaxMinutes(maxMinutes, difficulty);

        List<RecipeSummaryResponse> local = parseOffset(cursor) == 0
                ? localRecipeSearchService.searchByIngredients(userId, include, excluded, cuisineId, maxReadyMinutes, difficulty)
                : List.of();

        if (cuisine != null && !recipeProviderClient.supportsCuisine(cuisine.getName())) {
            return withLocal(local, this::emptyPage);
        }

        RecipeSearchCriteria criteria = new RecipeSearchCriteria(
                include,
                excluded,
                null,
                cuisine == null ? null : cuisine.getName(),
                maxReadyMinutes,
                RecipeSort.MIN_MISSING_INGREDIENTS);
        return withLocal(local, () -> searchPage(criteria, cursor, size, difficulty));
    }

    public CursorResponse<RecipeSummaryResponse> getSuggestions(String cursor, int size) {
        UUID userId = appUtil.userIdFromAuthentication();
        List<String> excluded = excludedNames(userId);
        List<String> include = pantryIngredientNames(userId, excluded);
        if (include.isEmpty()) {
            return emptyPage();
        }

        List<RecipeSummaryResponse> local = parseOffset(cursor) == 0
                ? localRecipeSearchService.searchByIngredients(userId, include, excluded, null, null, null)
                : List.of();

        RecipeSearchCriteria criteria = new RecipeSearchCriteria(
                include, excluded, null, null, null, RecipeSort.MIN_MISSING_INGREDIENTS);
        return withLocal(local, () -> searchPage(criteria, cursor, size, null));
    }

    public CursorResponse<RecipeSummaryResponse> searchByName(String keyword, String cursor, int size) {
        UUID userId = appUtil.userIdFromAuthentication();
        if (keyword == null || keyword.isBlank()) {
            throw new InvalidRecipeSearchException();
        }

        List<String> excluded = excludedNames(userId);
        List<RecipeSummaryResponse> local = parseOffset(cursor) == 0
                ? localRecipeSearchService.searchByName(userId, keyword, excluded)
                : List.of();

        RecipeSearchCriteria criteria = new RecipeSearchCriteria(
                List.of(), excluded, keyword.trim(), null, null, RecipeSort.RELEVANCE);
        return withLocal(local, () -> searchPage(criteria, cursor, size, null));
    }

    public List<RecipeSummaryResponse> getRandom(int count) {
        int limit = Math.min(Math.max(count, 1), MAX_RANDOM_COUNT);
        return toSummaries(recipeProviderClient.getRandom(limit));
    }

    public List<RecipeSummaryResponse> getSimilar(String externalId) {
        requireValidExternalId(externalId);
        return toSummaries(recipeProviderClient.getSimilar(externalId, SIMILAR_COUNT));
    }

    // ---------- chi tiết ----------

    /**
     * Trả công thức từ bản sao cục bộ nếu đã có; chưa có thì lấy từ API ngoài và lưu lại (nên response luôn có {@code id}).
     */
    public RecipeDetailResponse getByExternalId(String externalId) {
        requireValidExternalId(externalId);
        return recipeImportService.findImported(externalId)
                .orElseGet(() -> importFromProvider(externalId));
    }

    public RecipeDetailResponse scale(String externalId, int servings) {
        if (servings <= 0 || servings > MAX_SERVINGS) {
            throw new InvalidServingsException();
        }
        RecipeDetailResponse detail = getByExternalId(externalId);
        Integer baseServings = detail.getServings();
        if (baseServings == null || baseServings <= 0) {
            throw new InvalidServingsException();
        }

        double factor = (double) servings / baseServings;
        for (RecipeIngredientResponse ingredient : detail.getIngredients()) {
            if (ingredient.getAmount() != null) {
                ingredient.setAmount(Math.round(ingredient.getAmount() * factor * AMOUNT_ROUNDING) / AMOUNT_ROUNDING);
            }
        }
        detail.setServings(servings);
        return detail;
    }

    // ---------- private helpers ----------

    private RecipeDetailResponse importFromProvider(String externalId) {
        ExternalRecipeDetail external = recipeProviderClient.getDetail(externalId);
        try {
            return recipeImportService.importRecipe(external);
        } catch (DataIntegrityViolationException ex) {
            // request khác vừa lưu cùng công thức: đọc lại bản đã lưu
            log.info("External recipe {} was imported concurrently", externalId);
            return recipeImportService.findImported(externalId).orElseThrow(RecipeNotFoundException::new);
        }
    }

    /**
     * Ghép kết quả trong DB (chỉ có ở trang đầu) lên trước kết quả từ API ngoài, bỏ món ngoài đã có sẵn trong DB.
     * Nếu API ngoài lỗi hoặc hết quota mà DB có kết quả thì trả riêng kết quả DB thay vì báo lỗi.
     */
    private CursorResponse<RecipeSummaryResponse> withLocal(
            List<RecipeSummaryResponse> local, Supplier<CursorResponse<RecipeSummaryResponse>> external) {
        CursorResponse<RecipeSummaryResponse> remote;
        try {
            remote = external.get();
        } catch (RecipeProviderException | RecipeProviderQuotaExceededException ex) {
            if (local.isEmpty()) {
                throw ex;
            }
            log.warn("Recipe provider unavailable, returning local results only: {}", ex.getMessage());
            return CursorResponse.<RecipeSummaryResponse>builder()
                    .content(local).hasNext(false).nextCursor(null).build();
        }
        if (local.isEmpty()) {
            return remote;
        }

        Set<String> localExternalIds = local.stream()
                .map(RecipeSummaryResponse::getExternalId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<RecipeSummaryResponse> merged = new ArrayList<>(local);
        remote.getContent().stream()
                .filter(item -> !localExternalIds.contains(item.getExternalId()))
                .forEach(merged::add);

        return CursorResponse.<RecipeSummaryResponse>builder()
                .content(merged)
                .hasNext(remote.isHasNext())
                .nextCursor(remote.getNextCursor())
                .build();
    }

    private CursorResponse<RecipeSummaryResponse> searchPage(
            RecipeSearchCriteria criteria, String cursor, int size, Difficulty difficultyFilter) {
        int offset = parseOffset(cursor);
        int limit = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        ExternalRecipePage page = recipeProviderClient.search(criteria, offset, limit);

        int nextOffset = offset + page.getItems().size();
        boolean hasNext = nextOffset < page.getTotalResults() && nextOffset <= MAX_OFFSET;

        // Độ khó chỉ suy ra được sau khi nhận kết quả nên lọc ở đây: một trang có thể ít hơn size nhưng vẫn có trang kế
        List<RecipeSummaryResponse> content = toSummaries(page.getItems()).stream()
                .filter(summary -> difficultyFilter == null || summary.getDifficulty() == difficultyFilter)
                .toList();

        return CursorResponse.<RecipeSummaryResponse>builder()
                .content(content)
                .hasNext(hasNext)
                .nextCursor(hasNext ? String.valueOf(nextOffset) : null)
                .build();
    }

    private List<RecipeSummaryResponse> toSummaries(List<ExternalRecipeSummary> items) {
        Map<String, Cuisine> cuisinesByName = cuisineRepository.findAllByOrderByNameAsc().stream()
                .collect(Collectors.toMap(c -> c.getName().toLowerCase(Locale.ROOT), Function.identity(), (a, b) -> a));
        return items.stream().map(item -> toSummary(item, cuisinesByName)).toList();
    }

    private RecipeSummaryResponse toSummary(ExternalRecipeSummary item, Map<String, Cuisine> cuisinesByName) {
        Integer used = item.getUsedIngredientCount();
        Integer missed = item.getMissedIngredientCount();
        Double matchScore = (used == null || missed == null || used + missed == 0)
                ? null : (double) used / (used + missed);
        Cuisine cuisine = item.getCuisineName() == null ? null
                : cuisinesByName.get(item.getCuisineName().toLowerCase(Locale.ROOT));

        return RecipeSummaryResponse.builder()
                .externalId(item.getExternalId())
                .title(item.getTitle())
                .imageUrl(item.getImageUrl())
                .servings(item.getServings())
                .readyInMinutes(item.getReadyInMinutes())
                .difficulty(Difficulty.fromReadyInMinutes(item.getReadyInMinutes()))
                .custom(false)
                .cuisine(cuisine == null ? null : cuisineMapper.toCuisineResponse(cuisine))
                .usedIngredientCount(used)
                .missedIngredientCount(missed)
                .matchScore(matchScore)
                .missingIngredients(item.getMissedIngredients())
                .build();
    }

    private CursorResponse<RecipeSummaryResponse> emptyPage() {
        return CursorResponse.<RecipeSummaryResponse>builder()
                .content(List.of())
                .hasNext(false)
                .nextCursor(null)
                .build();
    }

    /** Cursor của dữ liệu từ API ngoài là offset dạng chuỗi số. */
    private int parseOffset(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            int offset = Integer.parseInt(cursor.trim());
            if (offset < 0 || offset > MAX_OFFSET) {
                throw new InvalidRecipeCursorException();
            }
            return offset;
        } catch (NumberFormatException ex) {
            throw new InvalidRecipeCursorException();
        }
    }

    /** Trần thời gian tối đa: lấy giá trị nhỏ hơn giữa maxMinutes của người dùng và giới hạn của mức độ khó. */
    private Integer effectiveMaxMinutes(Integer maxMinutes, Difficulty difficulty) {
        Integer difficultyCap = difficulty == null ? null : difficulty.getMaxMinutes();
        if (maxMinutes == null) {
            return difficultyCap;
        }
        return difficultyCap == null ? maxMinutes : Math.min(maxMinutes, difficultyCap);
    }

    private List<String> excludedNames(UUID userId) {
        return normalizeNames(excludedIngredientRepository.findByUserId(userId).stream()
                .map(ExcludedIngredient::getName)
                .toList());
    }

    /**
     * Nguyên liệu trong tủ còn số lượng, bỏ nguyên liệu loại trừ. Khi nhiều hơn mức tối đa thì ưu tiên món sắp hết hạn.
     */
    private List<String> pantryIngredientNames(UUID userId, Collection<String> excluded) {
        List<String> names = pantryItemRepository.findByUserIdNotIngredientCategory(userId).stream()
                .filter(item -> item.getQuantity() == null || item.getQuantity() > 0)
                .sorted(Comparator.comparing(PantryItem::getExpiryDate, Comparator.nullsLast(Comparator.<LocalDate>naturalOrder())))
                .map(item -> normalizeName(item.getIngredient().getName()))
                .filter(name -> !name.isEmpty() && !excluded.contains(name))
                .distinct()
                .limit(MAX_SEARCH_INGREDIENTS)
                .toList();
        return normalizeNames(names);
    }

    /** Chữ thường, bỏ trùng và sắp xếp để cùng một tập nguyên liệu luôn cho cùng một khóa cache. */
    private List<String> normalizeNames(Collection<String> names) {
        if (names == null) {
            return List.of();
        }
        return names.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeName)
                .filter(name -> !name.isEmpty())
                .distinct()
                .sorted()
                .toList();
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    /** Chặn id lạ trước khi đưa vào URL gọi API ngoài. */
    private void requireValidExternalId(String externalId) {
        if (externalId == null || !EXTERNAL_ID_PATTERN.matcher(externalId).matches()) {
            throw new RecipeNotFoundException();
        }
    }
}
