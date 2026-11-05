package org.group12.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.enums.Unit;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.entity.Role;
import org.group12.feature.auth.entity.User;
import org.group12.feature.auth.enums.MeasurementSystem;
import org.group12.feature.auth.enums.RoleName;
import org.group12.feature.auth.enums.ThemeMode;
import org.group12.feature.auth.repository.RoleRepository;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.cookbook.entity.SavedRecipe;
import org.group12.feature.cookbook.repository.CollectionRepository;
import org.group12.feature.cookbook.repository.SavedRecipeRepository;
import org.group12.feature.feedback.entity.Feedback;
import org.group12.feature.feedback.enums.FeedbackType;
import org.group12.feature.feedback.repository.FeedbackRepository;
import org.group12.feature.ingredient.entity.ExcludedIngredient;
import org.group12.feature.ingredient.entity.Ingredient;
import org.group12.feature.ingredient.entity.IngredientCategory;
import org.group12.feature.ingredient.repository.ExcludedIngredientRepository;
import org.group12.feature.ingredient.repository.IngredientCategoryRepository;
import org.group12.feature.ingredient.repository.IngredientRepository;
import org.group12.feature.mealplan.entity.MealPlan;
import org.group12.feature.mealplan.entity.MealPlanEntry;
import org.group12.feature.mealplan.enums.MealType;
import org.group12.feature.mealplan.repository.MealPlanRepository;
import org.group12.feature.pantry.entity.PantryItem;
import org.group12.feature.pantry.enums.StorageLocation;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.group12.feature.recipe.entity.Cuisine;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.entity.RecipeIngredient;
import org.group12.feature.recipe.entity.RecipeStep;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.recipe.repository.CuisineRepository;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.group12.feature.sysc.entity.SyncStatus;
import org.group12.feature.sysc.repository.SyncStatusRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Profile("dev")
@RequiredArgsConstructor
@Component
public class InitData implements CommandLineRunner {

    private static final String SEED_FILE = "seed/data.json";

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PantryItemRepository pantryItemRepository;
    private final SavedRecipeRepository savedRecipeRepository;
    private final CollectionRepository collectionRepository;
    private final AppUtil appUtil;
    private final RecipeRepository recipeRepository;
    private final CuisineRepository cuisineRepository;
    private final IngredientCategoryRepository ingredientCategoryRepository;
    private final IngredientRepository ingredientRepository;
    private final ExcludedIngredientRepository excludedIngredientRepository;
    private final SyncStatusRepository syncStatusRepository;
    private final MealPlanRepository mealPlanRepository;

    private final ObjectMapper mapper = new ObjectMapper();
    // key: "email|recipeExternalId", dùng cho initCollections
    private final Map<String, SavedRecipe> savedIndex = new HashMap<>();

    @Value("${app.password-default}")
    private String PASSWORD;

    @Override
    public void run(String @NonNull ... args) {

        SeedData data = readSeed();

        initRoles(data);
        initUsers(data);
        initCuisines(data);
        initIngredientCategories(data);
        initSyncStatuses(data);
        initIngredients(data);
        initExcludedIngredients(data);
        initRecipes(data);
        initPantryItems(data);
        initMealPlans(data);
        initSavedRecipes(data);
        initCollections(data);
        initFeedbacks(data);

        log.info("Data initialization completed successfully.");
    }

    // ---------- helpers ----------

    private SeedData readSeed() {
        try (InputStream in = new ClassPathResource(SEED_FILE).getInputStream()) {
            return mapper.readValue(in, SeedData.class);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read " + SEED_FILE, e);
        }
    }

    private Map<String, User> usersByEmail() {
        return userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getEmail, Function.identity()));
    }

    private Map<String, Recipe> recipesByExternalId() {
        return recipeRepository.findAll().stream()
                .filter(r -> r.getExternalId() != null)
                .collect(Collectors.toMap(Recipe::getExternalId, Function.identity()));
    }

    private Map<String, SyncStatus> syncStatusesByCode() {
        return syncStatusRepository.findAll().stream()
                .collect(Collectors.toMap(SyncStatus::getCode, Function.identity()));
    }

    // ---------- seeders ----------

    private void initRoles(SeedData data) {
        if (roleRepository.count() > 0) return;
        roleRepository.saveAll(data.getRoles().stream()
                .map(name -> Role.builder()
                        .id(appUtil.generateUUID())
                        .name(RoleName.valueOf(name))
                        .build())
                .toList());
    }

    private void initUsers(SeedData data) {
        if (userRepository.count() > 0) return;
        Map<String, Role> roles = roleRepository.findAll().stream()
                .collect(Collectors.toMap(r -> r.getName().name(), Function.identity()));

        userRepository.saveAll(data.getUsers().stream()
                .map(u -> User.builder()
                        .id(appUtil.generateUUID())
                        .email(u.getEmail())
                        .password(passwordEncoder.encode(u.getPassword() != null ? u.getPassword() : PASSWORD))
                        .displayName(u.getDisplayName())
                        .measurementSystem(MeasurementSystem.valueOf(u.getMeasurementSystem()))
                        .themeMode(ThemeMode.valueOf(u.getThemeMode()))
                        .roles(u.getRoles().stream().map(roles::get).collect(Collectors.toSet()))
                        .build())
                .toList());
    }

    private void initCuisines(SeedData data) {
        if (cuisineRepository.count() > 0) return;
        cuisineRepository.saveAll(data.getCuisines().stream()
                .map(c -> Cuisine.builder()
                        .id(appUtil.generateUUID())
                        .code(c.getCode())
                        .name(c.getName())
                        .build())
                .toList());
    }

    private void initIngredientCategories(SeedData data) {
        if (ingredientCategoryRepository.count() > 0) return;
        ingredientCategoryRepository.saveAll(data.getIngredientCategories().stream()
                .map(c -> IngredientCategory.builder()
                        .id(appUtil.generateUUID())
                        .code(c.getCode())
                        .name(c.getName())
                        .sortOrder(c.getSortOrder())
                        .build())
                .toList());
    }

    private void initSyncStatuses(SeedData data) {
        Set<String> existing = syncStatusRepository.findAll().stream()
                .map(SyncStatus::getCode)
                .collect(Collectors.toSet());
        List<SyncStatus> missing = data.getSyncStatuses().stream()
                .filter(s -> !existing.contains(s.getCode()))
                .map(s -> SyncStatus.builder()
                        .id(appUtil.generateUUID())
                        .code(s.getCode())
                        .name(s.getName())
                        .build())
                .toList();
        if (!missing.isEmpty()) {
            syncStatusRepository.saveAll(missing);
            log.info("Seeded {} sync statuses", missing.size());
        }
    }

    private void initIngredients(SeedData data) {
        if (ingredientRepository.count() > 0) return;
        Map<String, IngredientCategory> categories = ingredientCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(IngredientCategory::getCode, Function.identity()));

        List<Ingredient> list = new ArrayList<>();
        for (SeedData.IngredientSeed s : data.getIngredients()) {
            IngredientCategory category = categories.get(s.getCategory());
            if (category == null) {
                log.warn("Skip ingredient '{}': category {} not found", s.getName(), s.getCategory());
                continue;
            }
            list.add(Ingredient.builder()
                    .id(appUtil.generateUUID())
                    .name(s.getName())
                    .defaultUnit(Unit.valueOf(s.getDefaultUnit()))
                    .barcode(s.getBarcode())
                    .category(category)
                    .build());
        }
        ingredientRepository.saveAll(list);
        log.info("Seeded {} ingredients", list.size());
    }

    private void initExcludedIngredients(SeedData data) {
        if (excludedIngredientRepository.count() > 0) return;
        Map<String, User> users = usersByEmail();
        List<ExcludedIngredient> list = new ArrayList<>();
        for (SeedData.ExcludedIngredientSeed s : data.getExcludedIngredients()) {
            User user = users.get(s.getUserEmail());
            if (user == null) {
                log.warn("Skip excluded ingredient '{}': user {} not found", s.getName(), s.getUserEmail());
                continue;
            }
            list.add(ExcludedIngredient.builder()
                    .id(appUtil.generateUUID())
                    .name(s.getName())
                    .user(user)
                    .build());
        }
        excludedIngredientRepository.saveAll(list);
    }

    private void initRecipes(SeedData data) {
        if (recipeRepository.count() > 0) return;
        Map<String, Cuisine> cuisines = cuisineRepository.findAll().stream()
                .collect(Collectors.toMap(Cuisine::getCode, Function.identity()));

        List<Recipe> recipes = new ArrayList<>();
        for (SeedData.RecipeSeed s : data.getRecipes()) {
            Cuisine cuisine = cuisines.get(s.getCuisine());
            if (cuisine == null) {
                log.warn("Skip recipe '{}': cuisine {} not found", s.getTitle(), s.getCuisine());
                continue;
            }

            Recipe recipe = Recipe.builder()
                    .id(appUtil.generateUUID())
                    .externalId(s.getExternalId())
                    .title(s.getTitle())
                    .imageUrl(s.getImageUrl())
                    .servings(s.getServings())
                    .readyInMinutes(s.getReadyInMinutes())
                    .difficulty(Difficulty.valueOf(s.getDifficulty()))
                    .custom(false)
                    .cuisine(cuisine)
                    .build();

            for (SeedData.RecipeIngredientSeed ing : s.getIngredients()) {
                recipe.getIngredients().add(RecipeIngredient.builder()
                        .id(appUtil.generateUUID())
                        .recipe(recipe)
                        .name(ing.getName())
                        .amount(ing.getAmount())
                        .unit(Unit.valueOf(ing.getUnit()))
                        .build());
            }

            int order = 1;
            for (SeedData.RecipeStepSeed st : s.getSteps()) {
                recipe.getSteps().add(RecipeStep.builder()
                        .id(appUtil.generateUUID())
                        .recipe(recipe)
                        .stepOrder(order++)
                        .text(st.getText())
                        .timerSeconds(st.getTimerSeconds())
                        .build());
            }
            recipes.add(recipe);
        }
        recipeRepository.saveAll(recipes);
        log.info("Seeded {} recipes", recipes.size());
    }

    private void initPantryItems(SeedData data) {
        if (pantryItemRepository.count() > 0) return;
        Map<String, User> users = usersByEmail();
        Map<String, Ingredient> ingredients = ingredientRepository.findAll().stream()
                .collect(Collectors.toMap(Ingredient::getName, Function.identity()));
        Map<String, SyncStatus> statuses = syncStatusesByCode();
        LocalDate today = LocalDate.now();

        List<PantryItem> items = new ArrayList<>();
        for (SeedData.PantryItemSeed s : data.getPantryItems()) {
            User user = users.get(s.getUserEmail());
            Ingredient ingredient = ingredients.get(s.getIngredient());
            if (user == null || ingredient == null) {
                log.warn("Skip pantry item '{}' of {}: user or ingredient not found", s.getIngredient(), s.getUserEmail());
                continue;
            }
            items.add(PantryItem.builder()
                    .id(appUtil.generateUUID())
                    .user(user)
                    .ingredient(ingredient)
                    .quantity(s.getQuantity())
                    .unit(ingredient.getDefaultUnit())
                    .location(StorageLocation.valueOf(s.getLocation()))
                    .expiryDate(today.plusDays(s.getExpiryInDays()))
                    .syncStatus(statuses.get(s.getSyncStatus()))
                    .build());
        }
        pantryItemRepository.saveAll(items);
        log.info("Seeded {} pantry items", items.size());
    }

    private void initMealPlans(SeedData data) {
        if (mealPlanRepository.count() > 0) return;
        Map<String, User> users = usersByEmail();
        Map<String, Recipe> recipes = recipesByExternalId();
        LocalDate today = LocalDate.now();
        LocalDate thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<MealPlan> plans = new ArrayList<>();
        for (SeedData.MealPlanSeed p : data.getMealPlans()) {
            User user = users.get(p.getUserEmail());
            if (user == null) {
                log.warn("Skip meal plan: user {} not found", p.getUserEmail());
                continue;
            }
            MealPlan plan = MealPlan.builder()
                    .id(appUtil.generateUUID())
                    .weekStart(thisMonday.plusWeeks(p.getWeekOffset()))
                    .user(user)
                    .build();

            for (SeedData.MealPlanEntrySeed e : p.getEntries()) {
                Recipe recipe = recipes.get(e.getRecipeExternalId());
                if (recipe == null) {
                    log.warn("Skip meal entry: recipe {} not found", e.getRecipeExternalId());
                    continue;
                }
                LocalDate date = plan.getWeekStart().plusDays(e.getDayOffset());
                plan.getEntries().add(MealPlanEntry.builder()
                        .id(appUtil.generateUUID())
                        .mealPlan(plan)
                        .recipe(recipe)
                        .date(date)
                        .mealType(MealType.valueOf(e.getMealType()))
                        .servings(e.getServings())
                        .cooked(date.isBefore(today))
                        .build());
            }
            plans.add(plan);
        }
        mealPlanRepository.saveAll(plans);
        log.info("Seeded {} meal plans", plans.size());
    }

    private void initSavedRecipes(SeedData data) {
        if (savedRecipeRepository.count() > 0) return;
        Map<String, User> users = usersByEmail();
        Map<String, Recipe> recipes = recipesByExternalId();
        Map<String, SyncStatus> statuses = syncStatusesByCode();

        List<SavedRecipe> list = new ArrayList<>();
        for (SeedData.SavedRecipeSeed s : data.getSavedRecipes()) {
            User user = users.get(s.getUserEmail());
            Recipe recipe = recipes.get(s.getRecipeExternalId());
            if (user == null || recipe == null) {
                log.warn("Skip saved recipe {} of {}: user or recipe not found", s.getRecipeExternalId(), s.getUserEmail());
                continue;
            }
            SavedRecipe saved = SavedRecipe.builder()
                    .id(appUtil.generateUUID())
                    .user(user)
                    .recipe(recipe)
                    .rating(s.getRating())
                    .note(s.getNote())
                    .favorite(s.isFavorite())
                    .syncStatus(statuses.get(s.getSyncStatus()))
                    .build();
            list.add(saved);
            savedIndex.put(s.getUserEmail() + "|" + s.getRecipeExternalId(), saved);
        }
        savedRecipeRepository.saveAll(list);
        log.info("Seeded {} saved recipes", list.size());
    }

    private void initCollections(SeedData data) {
        if (collectionRepository.count() > 0) return;
        Map<String, User> users = usersByEmail();

        List<org.group12.feature.cookbook.entity.Collection> list = new ArrayList<>();
        for (SeedData.CollectionSeed c : data.getCollections()) {
            User user = users.get(c.getUserEmail());
            if (user == null) {
                log.warn("Skip collection '{}': user {} not found", c.getName(), c.getUserEmail());
                continue;
            }
            Set<SavedRecipe> members = new HashSet<>();
            for (String externalId : c.getRecipeExternalIds()) {
                SavedRecipe saved = savedIndex.get(c.getUserEmail() + "|" + externalId);
                if (saved == null) {
                    log.warn("Collection '{}': recipe {} is not in saved recipes", c.getName(), externalId);
                    continue;
                }
                members.add(saved);
            }
            list.add(org.group12.feature.cookbook.entity.Collection.builder()
                    .id(appUtil.generateUUID())
                    .name(c.getName())
                    .user(user)
                    .savedRecipes(members)
                    .build());
        }
        collectionRepository.saveAll(list);
        log.info("Seeded {} collections", list.size());
    }

    private void initFeedbacks(SeedData data) {
        if (feedbackRepository.count() > 0) return;
        Map<String, User> users = usersByEmail();

        List<Feedback> list = new ArrayList<>();
        for (SeedData.FeedbackSeed f : data.getFeedbacks()) {
            User user = users.get(f.getUserEmail());
            if (user == null) {
                log.warn("Skip feedback: user {} not found", f.getUserEmail());
                continue;
            }
            list.add(Feedback.builder()
                    .id(appUtil.generateUUID())
                    .type(FeedbackType.valueOf(f.getType()))
                    .message(f.getMessage())
                    .appVersion(f.getAppVersion())
                    .deviceInfo(f.getDeviceInfo())
                    .user(user)
                    .build());
        }
        feedbackRepository.saveAll(list);
        log.info("Seeded {} feedbacks", list.size());
    }
}