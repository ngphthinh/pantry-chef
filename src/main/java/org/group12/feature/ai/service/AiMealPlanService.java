package org.group12.feature.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.group12.common.config.ContentGuard;
import org.group12.common.config.PromptLoader;
import org.group12.common.utils.AppUtil;
import org.group12.feature.ai.dto.request.AiMealPlanRequest;
import org.group12.feature.ai.dto.response.AiMealPlanDraft;
import org.group12.feature.ai.dto.response.MealPlanResponse;
import org.group12.feature.ai.exception.AiProviderException;
import org.group12.feature.ai.mapper.MealPlanMapper;
import org.group12.feature.ingredient.entity.ExcludedIngredient;
import org.group12.feature.ingredient.repository.ExcludedIngredientRepository;
import org.group12.feature.mealplan.enums.MealType;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.group12.feature.recipe.entity.Cuisine;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.exception.CuisineNotFoundException;
import org.group12.feature.recipe.repository.CuisineRepository;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class AiMealPlanService {

    private static final Set<MealType> ALLOWED_MEALS =
            EnumSet.of(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER);

    private final ChatClient chatClient;
    private final PromptLoader promptLoader;
    private final ContentGuard contentGuard;
    private final PantryItemRepository pantryItemRepository;
    private final ExcludedIngredientRepository excludedIngredientRepository;
    private final CuisineRepository cuisineRepository;
    private final RecipeRepository recipeRepository;
    private final MealPlanMapper mealPlanMapper;
    private final AppUtil appUtil;
    private final String promptPath;
    private final int maxPantryItems;

    public AiMealPlanService(
            ChatClient.Builder builder,
            PromptLoader promptLoader,
            ContentGuard contentGuard,
            PantryItemRepository pantryItemRepository,
            ExcludedIngredientRepository excludedIngredientRepository,
            CuisineRepository cuisineRepository,
            RecipeRepository recipeRepository,
            MealPlanMapper mealPlanMapper,
            AppUtil appUtil,
            @Value("${app.ai.prompt.path.meal-plan}") String promptPath,
            @Value("${app.ai.meal-plan.max-pantry-items}") int maxPantryItems) {
        this.chatClient = builder.build();
        this.promptLoader = promptLoader;
        this.contentGuard = contentGuard;
        this.pantryItemRepository = pantryItemRepository;
        this.excludedIngredientRepository = excludedIngredientRepository;
        this.cuisineRepository = cuisineRepository;
        this.recipeRepository = recipeRepository;
        this.mealPlanMapper = mealPlanMapper;
        this.appUtil = appUtil;
        this.promptPath = promptPath;
        this.maxPantryItems = maxPantryItems;
    }

    public MealPlanResponse generate(AiMealPlanRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();

        List<String> pantry = pantryItemRepository.findByUserId(userId).stream()
                .map(p -> p.getIngredient().getName())
                .distinct()
                .limit(maxPantryItems)
                .toList();
        if (pantry.isEmpty()) {
//            throw new PantryEmptyException();                                // 400
            throw new IllegalArgumentException("Pantry is empty");  // 400
        }

        List<String> excluded = excludedIngredientRepository.findByUserId(userId).stream()
                .map(ExcludedIngredient::getName)
                .toList();

        String cuisine = request.getCuisineId() == null ? "any"
                : cuisineRepository.findById(request.getCuisineId())
                .map(Cuisine::getName)
                .orElseThrow(CuisineNotFoundException::new);

        contentGuard.checkText(pantry.toArray(String[]::new));
        contentGuard.checkText(excluded.toArray(String[]::new));             // 422

        String template = promptLoader.load(promptPath);
        String maxMinutes = request.getMaxMinutes() == null ? "no limit" : request.getMaxMinutes().toString();

        AiMealPlanDraft draft;
        try {
            draft = chatClient.prompt()
                    .system(contentGuard.systemPrompt())
                    .user(u -> u.text(template)
                            .param("pantry", String.join(", ", pantry))
                            .param("excluded", excluded.isEmpty() ? "none" : String.join(", ", excluded))
                            .param("cuisine", cuisine)
                            .param("maxMinutes", maxMinutes)
                            .param("days", String.valueOf(request.getDays())))
                    .call()
                    .entity(AiMealPlanDraft.class);
        } catch (Exception e) {
            log.error("Meal plan generation failed", e);
            throw new AiProviderException();                                 // 502
        }

        if (draft == null || draft.getItems() == null) {
            return MealPlanResponse.empty(request.getWeekStart());
        }

        List<AiMealPlanDraft.Item> valid = filterValid(draft.getItems(), request, excluded);

        contentGuard.checkOutput(valid.stream()
                .flatMap(i -> Stream.concat(
                        Stream.of(i.getRecipeTitle()),
                        i.getUsedIngredients() == null
                                ? Stream.<String>empty() : i.getUsedIngredients().stream()))
                .toList());                                                  // 422

        return buildResponse(request.getWeekStart(), valid);
    }

    private List<AiMealPlanDraft.Item> filterValid(
            List<AiMealPlanDraft.Item> items, AiMealPlanRequest request, List<String> excluded) {

        Set<String> excludedLower = excluded.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> seen = new HashSet<>();

        return items.stream()
                .filter(Objects::nonNull)
                .filter(i -> i.getRecipeTitle() != null && !i.getRecipeTitle().isBlank())
                .filter(i -> i.getDayIndex() != null
                        && i.getDayIndex() >= 0 && i.getDayIndex() < request.getDays())
                .filter(i -> i.getMealType() != null && ALLOWED_MEALS.contains(i.getMealType()))
                .filter(i -> request.getMaxMinutes() == null || i.getReadyInMinutes() == null
                        || i.getReadyInMinutes() <= request.getMaxMinutes())
                .filter(i -> i.getUsedIngredients() == null || i.getUsedIngredients().stream()
                        .noneMatch(n -> n != null && excludedLower.contains(n.toLowerCase(Locale.ROOT))))
                .filter(i -> seen.add(i.getDayIndex() + ":" + i.getMealType()))  // mỗi (ngày, bữa) tối đa 1 món
                .toList();
    }

    private MealPlanResponse buildResponse(Instant weekStart, List<AiMealPlanDraft.Item> items) {
        Set<String> titles = items.stream()
                .map(i -> i.getRecipeTitle().strip().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        Map<String, UUID> recipeIdByTitle = titles.isEmpty() ? Map.of()
                : recipeRepository.findByLowerTitleIn(titles).stream()
                .collect(Collectors.toMap(
                        r -> r.getTitle().strip().toLowerCase(Locale.ROOT),
                        Recipe::getId,
                        (a, b) -> a));                               // trùng tên: lấy cái đầu

        List<MealPlanResponse.Entry> entries = items.stream()
                .sorted(Comparator.comparing(AiMealPlanDraft.Item::getDayIndex)
                        .thenComparing(AiMealPlanDraft.Item::getMealType))   // theo thứ tự khai báo enum
                .map(i -> mealPlanMapper.toEntry(
                        i,
                        weekStart,
                        recipeIdByTitle.get(i.getRecipeTitle().strip().toLowerCase(Locale.ROOT))))
                .toList();

        return mealPlanMapper.toMealPlanResponse(weekStart, entries);
    }
}