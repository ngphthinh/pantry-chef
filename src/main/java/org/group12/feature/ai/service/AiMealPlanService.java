package org.group12.feature.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.group12.common.config.ContentGuard;
import org.group12.common.config.PromptLoader;
import org.group12.common.utils.AppUtil;
import org.group12.feature.ai.dto.request.AiMealPlanRequest;
import org.group12.feature.ai.dto.response.AiMealPlanDraft;
import org.group12.feature.ai.dto.response.MealPlanResponse;
import org.group12.feature.ai.exception.AiProviderException;
import org.group12.feature.ai.mapper.AiMealPlanMapper;
import org.group12.feature.ingredient.entity.ExcludedIngredient;
import org.group12.feature.ingredient.repository.ExcludedIngredientRepository;
import org.group12.feature.mealplan.enums.MealType;
import org.group12.feature.pantry.exception.PantryEmptyException;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiMealPlanService {

    private static final Set<MealType> ALLOWED_MEALS =
            EnumSet.of(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER);
    private static final int MAX_CANDIDATES = 60;

    private final ChatClient chatClient;
    private final PromptLoader promptLoader;
    private final ContentGuard contentGuard;
    private final PantryItemRepository pantryItemRepository;
    private final ExcludedIngredientRepository excludedIngredientRepository;
    private final RecipeRepository recipeRepository;
    private final AiMealPlanMapper mealPlanMapper;
    private final AppUtil appUtil;
    private final String promptPath;
    private final int maxPantryItems;

    public AiMealPlanService(
            ChatClient.Builder builder,
            PromptLoader promptLoader,
            ContentGuard contentGuard,
            PantryItemRepository pantryItemRepository,
            ExcludedIngredientRepository excludedIngredientRepository,
            RecipeRepository recipeRepository,
            AiMealPlanMapper mealPlanMapper,
            AppUtil appUtil,
            @Value("${app.ai.prompt.path.meal-plan}") String promptPath,
            @Value("${app.ai.meal-plan.max-pantry-items}") int maxPantryItems) {
        this.chatClient = builder.build();
        this.promptLoader = promptLoader;
        this.contentGuard = contentGuard;
        this.pantryItemRepository = pantryItemRepository;
        this.excludedIngredientRepository = excludedIngredientRepository;
        this.recipeRepository = recipeRepository;
        this.mealPlanMapper = mealPlanMapper;
        this.appUtil = appUtil;
        this.promptPath = promptPath;
        this.maxPantryItems = maxPantryItems;
    }

    @PreAuthorize("hasRole('USER')")
    public MealPlanResponse generate(AiMealPlanRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();
        Instant weekStart = request.getWeekStart().truncatedTo(ChronoUnit.DAYS);   // quy ước 00:00:00Z

        List<String> pantry = pantryItemRepository.findByUserId(userId).stream()
                .map(p -> p.getIngredient().getName())
                .distinct()
                .limit(maxPantryItems)
                .toList();
        if (pantry.isEmpty()) {
            throw new PantryEmptyException();
        }

        List<String> excluded = excludedIngredientRepository.findByUserId(userId).stream()
                .map(ExcludedIngredient::getName)
                .toList();
        contentGuard.checkText(pantry.toArray(String[]::new));

        // recipe dùng được: không phải custom của người khác, đúng cuisine/thời gian, không chứa nguyên liệu loại trừ
        List<String> excludedLower = excluded.isEmpty() ? List.of("")
                : excluded.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
        Map<UUID, Recipe> candidates = recipeRepository
                .findCandidates(userId, request.getCuisineId(), request.getMaxMinutes(),
                        excludedLower, PageRequest.of(0, MAX_CANDIDATES))
                .stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity()));
        if (candidates.isEmpty()) {
            return MealPlanResponse.empty(weekStart);
        }

        String recipesText = candidates.values().stream()
                .map(r -> r.getId() + " | " + r.getTitle() + " | " + r.getReadyInMinutes())
                .collect(Collectors.joining("\n"));
        String template = promptLoader.load(promptPath);

        AiMealPlanDraft draft;
        try {
            draft = chatClient.prompt()
                    .system(contentGuard.systemPrompt())
                    .user(u -> u.text(template)
                            .param("pantry", String.join(", ", pantry))
                            .param("days", String.valueOf(request.getDays()))
                            .param("recipes", recipesText))
                    .call()
                    .entity(AiMealPlanDraft.class);
        } catch (Exception e) {
            log.error("Meal plan generation failed", e);
            throw new AiProviderException();
        }

        if (draft == null || draft.getItems() == null) {
            return MealPlanResponse.empty(weekStart);
        }
        return buildResponse(weekStart, request.getDays(), draft.getItems(), candidates);
    }

    private MealPlanResponse buildResponse(Instant weekStart, int days,
                                           List<AiMealPlanDraft.Item> items, Map<UUID, Recipe> candidates) {
        Set<String> seen = new HashSet<>();
        List<MealPlanResponse.Entry> entries = items.stream()
                .filter(Objects::nonNull)
                .filter(i -> i.getDayIndex() != null && i.getDayIndex() >= 0 && i.getDayIndex() < days)
                .filter(i -> i.getMealType() != null && ALLOWED_MEALS.contains(i.getMealType()))
                .filter(i -> seen.add(i.getDayIndex() + ":" + i.getMealType()))   // mỗi (ngày, bữa) tối đa 1 món
                .sorted(Comparator.comparing(AiMealPlanDraft.Item::getDayIndex)
                        .thenComparing(AiMealPlanDraft.Item::getMealType))
                .map(i -> parseUuid(i.getRecipeId())
                        .map(candidates::get)                                     // id ngoài danh sách → bị loại
                        .map(recipe -> mealPlanMapper.toEntry(i, weekStart, recipe))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        contentGuard.checkOutput(entries.stream()
                .map(MealPlanResponse.Entry::getRecipeTitle)
                .toList());                                                       // 422

        return mealPlanMapper.toMealPlanResponse(weekStart, entries);
    }

    private Optional<UUID> parseUuid(String s) {
        if (s == null) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(s.strip()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}