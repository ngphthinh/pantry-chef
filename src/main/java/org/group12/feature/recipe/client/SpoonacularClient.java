package org.group12.feature.recipe.client;

import lombok.extern.slf4j.Slf4j;
import org.group12.common.enums.Unit;
import org.group12.feature.recipe.client.SpoonacularPayload.ExtendedIngredient;
import org.group12.feature.recipe.client.SpoonacularPayload.Information;
import org.group12.feature.recipe.client.SpoonacularPayload.Instruction;
import org.group12.feature.recipe.client.SpoonacularPayload.InstructionStep;
import org.group12.feature.recipe.client.SpoonacularPayload.Measure;
import org.group12.feature.recipe.client.SpoonacularPayload.NamedItem;
import org.group12.feature.recipe.client.SpoonacularPayload.RandomResponse;
import org.group12.feature.recipe.client.SpoonacularPayload.SearchItem;
import org.group12.feature.recipe.client.SpoonacularPayload.SearchResponse;
import org.group12.feature.recipe.client.SpoonacularPayload.SimilarItem;
import org.group12.feature.recipe.client.SpoonacularPayload.StepLength;
import org.group12.feature.recipe.client.model.ExternalRecipeDetail;
import org.group12.feature.recipe.client.model.ExternalRecipeIngredient;
import org.group12.feature.recipe.client.model.ExternalRecipePage;
import org.group12.feature.recipe.client.model.ExternalRecipeStep;
import org.group12.feature.recipe.client.model.ExternalRecipeSummary;
import org.group12.feature.recipe.client.model.RecipeSearchCriteria;
import org.group12.feature.recipe.config.RecipeCacheConfig;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.exception.RecipeProviderException;
import org.group12.feature.recipe.exception.RecipeProviderQuotaExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.group12.common.exception.AppException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Cài đặt {@link RecipeProviderClient} cho Spoonacular (https://spoonacular.com/food-api/docs).
 * Khóa API gửi qua header {@code x-api-key}, không nằm trong URL nên không lọt vào log.
 */
@Slf4j
@Component
public class SpoonacularClient implements RecipeProviderClient {

    private static final String API_KEY_HEADER = "x-api-key";
    private static final String SEARCH_PATH = "/recipes/complexSearch";
    private static final String DETAIL_PATH = "/recipes/{recipeId}/information";
    private static final String SIMILAR_PATH = "/recipes/{recipeId}/similar";
    private static final String RANDOM_PATH = "/recipes/random";
    private static final String PATH_VAR_RECIPE_ID = "recipeId";
    private static final String IMAGE_URL_TEMPLATE = "https://img.spoonacular.com/recipes/%s-312x231.%s";

    private static final String SORT_MIN_MISSING = "min-missing-ingredients";
    private static final String DIRECTION_ASC = "asc";

    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_PAYMENT_REQUIRED = 402;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    /** Các giá trị tham số {@code cuisine} Spoonacular hỗ trợ (chữ thường). */
    private static final Set<String> SUPPORTED_CUISINES = Set.of(
            "african", "american", "asian", "british", "cajun", "caribbean", "chinese", "eastern european",
            "european", "french", "german", "greek", "indian", "irish", "italian", "japanese", "jewish",
            "korean", "latin american", "mediterranean", "mexican", "middle eastern", "nordic", "southern",
            "spanish", "thai", "vietnamese");

    private final RestClient restClient;
    private final String apiKey;

    public SpoonacularClient(RestClient restClient, @Value("${app.recipe.provider.api-key}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    @Override
    public boolean supportsCuisine(String cuisineName) {
        return cuisineName != null && SUPPORTED_CUISINES.contains(cuisineName.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    @Cacheable(cacheNames = RecipeCacheConfig.SEARCH_CACHE,
            key = "#criteria.toString() + ':' + #offset + ':' + #number")
    public ExternalRecipePage search(RecipeSearchCriteria criteria, int offset, int number) {
        Map<String, Object> query = new LinkedHashMap<>();
        putIfNotBlank(query, "query", criteria.query());
        if (!criteria.includeIngredients().isEmpty()) {
            query.put("includeIngredients", String.join(",", criteria.includeIngredients()));
            query.put("fillIngredients", true);
        }
        if (!criteria.excludeIngredients().isEmpty()) {
            query.put("excludeIngredients", String.join(",", criteria.excludeIngredients()));
        }
        putIfNotBlank(query, "cuisine", criteria.cuisineName());
        if (criteria.maxReadyMinutes() != null) {
            query.put("maxReadyTime", criteria.maxReadyMinutes());
        }
        switch (criteria.sort()) {
            case MIN_MISSING_INGREDIENTS -> {
                query.put("sort", SORT_MIN_MISSING);
                query.put("sortDirection", DIRECTION_ASC);
            }
            case RELEVANCE -> {
                // mặc định của nhà cung cấp
            }
        }
        query.put("addRecipeInformation", true);
        query.put("offset", offset);
        query.put("number", number);

        SearchResponse response = get(SEARCH_PATH, Map.of(), query, SearchResponse.class);
        List<ExternalRecipeSummary> items = response.results() == null ? List.of()
                : response.results().stream().filter(item -> item.id() != null).map(this::toSummary).toList();
        int total = response.totalResults() == null ? offset + items.size() : response.totalResults();
        return new ExternalRecipePage(items, total);
    }

    @Override
    @Cacheable(cacheNames = RecipeCacheConfig.DETAIL_CACHE, key = "#externalId")
    public ExternalRecipeDetail getDetail(String externalId) {
        Information information = get(DETAIL_PATH, Map.of(PATH_VAR_RECIPE_ID, externalId),
                Map.of("includeNutrition", false), Information.class);
        return toDetail(information);
    }

    @Override
    @Cacheable(cacheNames = RecipeCacheConfig.SIMILAR_CACHE, key = "#externalId + ':' + #number")
    public List<ExternalRecipeSummary> getSimilar(String externalId, int number) {
        SimilarItem[] items = get(SIMILAR_PATH, Map.of(PATH_VAR_RECIPE_ID, externalId),
                Map.of("number", number), SimilarItem[].class);
        return java.util.Arrays.stream(items)
                .filter(item -> item.id() != null)
                .map(this::toSummary)
                .toList();
    }

    /** Không cache: mỗi lần gọi phải ra kết quả khác nhau. */
    @Override
    public List<ExternalRecipeSummary> getRandom(int number) {
        RandomResponse response = get(RANDOM_PATH, Map.of(), Map.of("number", number), RandomResponse.class);
        if (response.recipes() == null) {
            return List.of();
        }
        return response.recipes().stream()
                .filter(info -> info.id() != null)
                .map(info -> new ExternalRecipeSummary(String.valueOf(info.id()), info.title(), info.image(),
                        info.readyInMinutes(), info.servings(), firstOrNull(info.cuisines()), null, null, List.of()))
                .toList();
    }

    // ---------- HTTP ----------

    private <T> T get(String path, Map<String, Object> pathVariables, Map<String, Object> query, Class<T> type) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Recipe provider API key is not configured (SPOONACULAR_API_KEY)");
            throw new RecipeProviderException();
        }

        Map<String, Object> variables = new HashMap<>(pathVariables);
        variables.putAll(query);

        try {
            // Giá trị đi qua URI variable để được mã hóa chặt, tránh ký tự như { } trong tên nguyên liệu bị hiểu là template
            T body = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(path);
                        query.keySet().forEach(name -> uriBuilder.queryParam(name, "{" + name + "}"));
                        return uriBuilder.build(variables);
                    })
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .body(type);
            if (body == null) {
                throw new RecipeProviderException();
            }
            return body;
        } catch (RestClientResponseException ex) {
            throw translate(ex);
        } catch (RestClientException ex) {
            log.warn("Recipe provider call failed: {}", ex.getMessage());
            throw new RecipeProviderException();
        }
    }

    private AppException translate(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        if (status == HTTP_NOT_FOUND) {
            return new RecipeNotFoundException();
        }
        if (status == HTTP_PAYMENT_REQUIRED || status == HTTP_TOO_MANY_REQUESTS) {
            log.warn("Recipe provider quota exceeded (HTTP {})", status);
            return new RecipeProviderQuotaExceededException();
        }
        log.warn("Recipe provider returned HTTP {}", status);
        return new RecipeProviderException();
    }

    // ---------- mapping ----------

    private ExternalRecipeSummary toSummary(SearchItem item) {
        List<String> missed = item.missedIngredients() == null ? List.of()
                : item.missedIngredients().stream().map(NamedItem::name).filter(n -> n != null && !n.isBlank()).toList();
        return new ExternalRecipeSummary(String.valueOf(item.id()), item.title(), item.image(),
                item.readyInMinutes(), item.servings(), firstOrNull(item.cuisines()),
                item.usedIngredientCount(), item.missedIngredientCount(), missed);
    }

    private ExternalRecipeSummary toSummary(SimilarItem item) {
        String image = item.imageType() == null ? null
                : IMAGE_URL_TEMPLATE.formatted(item.id(), item.imageType());
        return new ExternalRecipeSummary(String.valueOf(item.id()), item.title(), image,
                item.readyInMinutes(), item.servings(), null, null, null, List.of());
    }

    private ExternalRecipeDetail toDetail(Information info) {
        List<ExternalRecipeIngredient> ingredients = new ArrayList<>();
        if (info.extendedIngredients() != null) {
            for (ExtendedIngredient ingredient : info.extendedIngredients()) {
                toIngredient(ingredient).ifPresent(ingredients::add);
            }
        }

        List<ExternalRecipeStep> steps = new ArrayList<>();
        if (info.analyzedInstructions() != null) {
            for (Instruction instruction : info.analyzedInstructions()) {
                if (instruction.steps() == null) {
                    continue;
                }
                for (InstructionStep step : instruction.steps()) {
                    if (step.step() != null && !step.step().isBlank()) {
                        steps.add(new ExternalRecipeStep(steps.size() + 1, step.step().trim(), toSeconds(step.length())));
                    }
                }
            }
        }
        if (steps.isEmpty() && info.instructions() != null && !info.instructions().isBlank()) {
            steps.add(new ExternalRecipeStep(1, stripHtml(info.instructions()), null));
        }

        return new ExternalRecipeDetail(String.valueOf(info.id()), info.title(), info.image(), info.servings(),
                info.readyInMinutes(), info.sourceUrl(), firstOrNull(info.cuisines()), ingredients, steps);
    }

    private java.util.Optional<ExternalRecipeIngredient> toIngredient(ExtendedIngredient ingredient) {
        String name = ingredient.name() != null && !ingredient.name().isBlank() ? ingredient.name() : ingredient.original();
        if (name == null || name.isBlank()) {
            return java.util.Optional.empty();
        }
        Measure metric = ingredient.measures() == null ? null : ingredient.measures().metric();
        if (metric != null && metric.amount() != null) {
            return java.util.Optional.of(new ExternalRecipeIngredient(name.trim(), metric.amount(), toUnit(metric.unitShort())));
        }
        return java.util.Optional.of(new ExternalRecipeIngredient(name.trim(), ingredient.amount(), toUnit(ingredient.unit())));
    }

    /** Đơn vị không có trong {@link Unit} (clove, slice, large, ...) được xem là đếm theo cái (PCS). */
    static Unit toUnit(String unit) {
        if (unit == null) {
            return Unit.PCS;
        }
        return switch (unit.trim().toLowerCase(Locale.ROOT)) {
            case "mg", "milligram", "milligrams" -> Unit.MG;
            case "g", "gram", "grams" -> Unit.G;
            case "kg", "kilogram", "kilograms" -> Unit.KG;
            case "ml", "milliliter", "milliliters", "millilitre", "millilitres" -> Unit.ML;
            case "l", "liter", "liters", "litre", "litres" -> Unit.L;
            case "tbsp", "tbsps", "tablespoon", "tablespoons" -> Unit.TBSP;
            case "tsp", "tsps", "teaspoon", "teaspoons" -> Unit.TSP;
            case "cup", "cups" -> Unit.CUP;
            case "pinch", "pinches" -> Unit.PINCH;
            default -> Unit.PCS;
        };
    }

    private Integer toSeconds(StepLength length) {
        if (length == null || length.number() == null || length.unit() == null) {
            return null;
        }
        return switch (length.unit().trim().toLowerCase(Locale.ROOT)) {
            case "seconds", "second" -> length.number();
            case "minutes", "minute" -> length.number() * SECONDS_PER_MINUTE;
            case "hours", "hour" -> length.number() * SECONDS_PER_HOUR;
            default -> null;
        };
    }

    private static String stripHtml(String html) {
        return html.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    private static String firstOrNull(List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private static void putIfNotBlank(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }
}
