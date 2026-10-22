package org.group12.common.config;

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
import org.group12.feature.ingredient.entity.Ingredient;
import org.group12.feature.ingredient.entity.IngredientCategory;
import org.group12.feature.ingredient.repository.IngredientCategoryRepository;
import org.group12.feature.ingredient.repository.IngredientRepository;
import org.group12.feature.mealplan.entity.MealPlan;
import org.group12.feature.mealplan.entity.MealPlanEntry;
import org.group12.feature.mealplan.enums.MealType;
import org.group12.feature.mealplan.repository.MealPlanRepository;
import org.group12.feature.pantry.entity.PantryItem;
import org.group12.feature.pantry.enums.StorageLocation;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.entity.RecipeIngredient;
import org.group12.feature.recipe.entity.RecipeStep;
import org.group12.feature.recipe.enums.Difficulty;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Bộ dữ liệu mẫu với ID CỐ ĐỊNH (trừ admin) để test Postman.
 *
 * Quy ước ID:
 *   Role              00000001-0000-0000-0000-0000000000NN
 *   IngredientCategory 00000002-0000-0000-0000-0000000000NN
 *   Ingredient        00000003-0000-0000-0000-0000000000NN
 *   Recipe            00000004-0000-0000-0000-0000000000NN
 *   RecipeIngredient  00000005-0000-0000-00RR-0000000000NN  (RR = số recipe)
 *   RecipeStep        00000006-0000-0000-00RR-0000000000NN  (RR = số recipe)
 *   PantryItem        00000007-0000-0000-0000-0000000000NN
 *   MealPlan          00000008-0000-0000-0000-0000000000NN
 *   MealPlanEntry     00000009-0000-0000-00PP-0000000000NN  (PP = số meal plan)
 */
@Slf4j
@Profile("dev")
@RequiredArgsConstructor
@Component
public class InitData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUtil appUtil;

    private final IngredientCategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final PantryItemRepository pantryItemRepository;
    private final MealPlanRepository mealPlanRepository;

    // ID cố định của meal plan và các entry (mỗi tuần 10 entry)
    private static final String MEAL_PLAN_THIS_WEEK = "00000008-0000-0000-0000-000000000001";
    private static final String MEAL_PLAN_NEXT_WEEK = "00000008-0000-0000-0000-000000000002";

    private static final String[] ENTRY_IDS_THIS_WEEK = {
            "00000009-0000-0000-0001-000000000001",
            "00000009-0000-0000-0001-000000000002",
            "00000009-0000-0000-0001-000000000003",
            "00000009-0000-0000-0001-000000000004",
            "00000009-0000-0000-0001-000000000005",
            "00000009-0000-0000-0001-000000000006",
            "00000009-0000-0000-0001-000000000007",
            "00000009-0000-0000-0001-000000000008",
            "00000009-0000-0000-0001-000000000009",
            "00000009-0000-0000-0001-000000000010"
    };

    private static final String[] ENTRY_IDS_NEXT_WEEK = {
            "00000009-0000-0000-0002-000000000001",
            "00000009-0000-0000-0002-000000000002",
            "00000009-0000-0000-0002-000000000003",
            "00000009-0000-0000-0002-000000000004",
            "00000009-0000-0000-0002-000000000005",
            "00000009-0000-0000-0002-000000000006",
            "00000009-0000-0000-0002-000000000007",
            "00000009-0000-0000-0002-000000000008",
            "00000009-0000-0000-0002-000000000009",
            "00000009-0000-0000-0002-000000000010"
    };

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initRoles();
        User admin = initAdmin();
        Map<String, IngredientCategory> categories = initIngredientCategories();
        Map<String, Ingredient> ingredients = initIngredients(categories);
        List<Recipe> recipes = initRecipes();
        initPantryItems(admin, ingredients);
        initMealPlans(admin, recipes);
    }

    // ------------------------------------------------------------------
    // Roles & Admin
    // ------------------------------------------------------------------

    private void initRoles() {
        if (roleRepository.count() == 0) {
            List<Role> roles = List.of(
                    Role.builder().id(UUID.fromString("00000001-0000-0000-0000-000000000001")).name(RoleName.ROLE_ADMIN).build(),
                    Role.builder().id(UUID.fromString("00000001-0000-0000-0000-000000000002")).name(RoleName.ROLE_USER).build()
            );
            roleRepository.saveAll(roles);
            log.info("Initialized roles: ADMIN, USER");
        }
    }

    private User initAdmin() {
        Optional<User> existing = userRepository.findByEmail("admin@gmail.com");
        if (existing.isPresent()) {
            return existing.get();
        }

        User user = User.builder()
                .email("admin@gmail.com")
                .password(passwordEncoder.encode("Admin@123"))
                .displayName("Admin Trường")
                .measurementSystem(MeasurementSystem.METRIC)
                .id(appUtil.generateUUID()) // admin giữ UUID ngẫu nhiên
                .themeMode(ThemeMode.LIGHT)
                .roles(Set.copyOf(roleRepository.findAll()))
                .build();
        user = userRepository.save(user);
        log.info("Initialized admin account: admin@gmail.com / Admin@123");
        return user;
    }

    // ------------------------------------------------------------------
    // Ingredient categories & ingredients
    // ------------------------------------------------------------------

    private Map<String, IngredientCategory> initIngredientCategories() {
        Map<String, IngredientCategory> map = new HashMap<>();
        if (categoryRepository.count() == 0) {
            List<IngredientCategory> categories = List.of(
                    IngredientCategory.builder().id(UUID.fromString("00000002-0000-0000-0000-000000000001")).code("VEGETABLE").name("Rau củ").build(),
                    IngredientCategory.builder().id(UUID.fromString("00000002-0000-0000-0000-000000000002")).code("MEAT_EGG").name("Thịt & Trứng").build(),
                    IngredientCategory.builder().id(UUID.fromString("00000002-0000-0000-0000-000000000003")).code("SEAFOOD").name("Hải sản").build(),
                    IngredientCategory.builder().id(UUID.fromString("00000002-0000-0000-0000-000000000004")).code("SPICE").name("Gia vị").build(),
                    IngredientCategory.builder().id(UUID.fromString("00000002-0000-0000-0000-000000000005")).code("GRAIN").name("Đồ khô & Tinh bột").build()
            );
            categoryRepository.saveAll(categories).forEach(c -> map.put(c.getCode(), c));
            log.info("Initialized {} ingredient categories", categories.size());
        } else {
            categoryRepository.findAll().forEach(c -> map.put(c.getCode(), c));
        }
        return map;
    }

    private Map<String, Ingredient> initIngredients(Map<String, IngredientCategory> categories) {
        Map<String, Ingredient> map = new HashMap<>();
        if (ingredientRepository.count() == 0) {
            IngredientCategory veg = categories.get("VEGETABLE");
            IngredientCategory meat = categories.get("MEAT_EGG");
            IngredientCategory seafood = categories.get("SEAFOOD");
            IngredientCategory spice = categories.get("SPICE");
            IngredientCategory grain = categories.get("GRAIN");

            List<Ingredient> ingredients = List.of(
                    ing("00000003-0000-0000-0000-000000000001", "Thịt bò", Unit.G, meat),
                    ing("00000003-0000-0000-0000-000000000002", "Thịt heo", Unit.G, meat),
                    ing("00000003-0000-0000-0000-000000000003", "Thịt gà", Unit.G, meat),
                    ing("00000003-0000-0000-0000-000000000004", "Trứng gà", Unit.PCS, meat),
                    ing("00000003-0000-0000-0000-000000000005", "Tôm", Unit.G, seafood),
                    ing("00000003-0000-0000-0000-000000000006", "Cá hồi", Unit.G, seafood),
                    ing("00000003-0000-0000-0000-000000000007", "Cà chua", Unit.PCS, veg),
                    ing("00000003-0000-0000-0000-000000000008", "Hành tây", Unit.PCS, veg),
                    ing("00000003-0000-0000-0000-000000000009", "Rau muống", Unit.G, veg),
                    ing("00000003-0000-0000-0000-000000000010", "Khoai tây", Unit.PCS, veg),
                    ing("00000003-0000-0000-0000-000000000011", "Cà rốt", Unit.PCS, veg),
                    ing("00000003-0000-0000-0000-000000000012", "Hành lá", Unit.G, veg),
                    ing("00000003-0000-0000-0000-000000000013", "Tỏi", Unit.G, veg),
                    ing("00000003-0000-0000-0000-000000000014", "Gừng", Unit.G, veg),
                    ing("00000003-0000-0000-0000-000000000015", "Nước mắm", Unit.ML, spice),
                    ing("00000003-0000-0000-0000-000000000016", "Dầu ăn", Unit.ML, spice),
                    ing("00000003-0000-0000-0000-000000000017", "Tiêu", Unit.G, spice),
                    ing("00000003-0000-0000-0000-000000000018", "Muối", Unit.G, spice),
                    ing("00000003-0000-0000-0000-000000000019", "Đường", Unit.G, spice),
                    ing("00000003-0000-0000-0000-000000000020", "Gạo", Unit.KG, grain),
                    ing("00000003-0000-0000-0000-000000000021", "Mì gói", Unit.PCS, grain),
                    ing("00000003-0000-0000-0000-000000000022", "Bún", Unit.G, grain)
            );
            ingredientRepository.saveAll(ingredients).forEach(i -> map.put(i.getName(), i));
            log.info("Initialized {} sample ingredients", ingredients.size());
        } else {
            ingredientRepository.findAll().forEach(i -> map.put(i.getName(), i));
        }
        return map;
    }

    private Ingredient ing(String id, String name, Unit unit, IngredientCategory category) {
        return Ingredient.builder()
                .id(UUID.fromString(id))
                .name(name)
                .defaultUnit(unit)
                .category(category)
                .build();
    }

    // ------------------------------------------------------------------
    // Recipes
    // ------------------------------------------------------------------

    private List<Recipe> initRecipes() {
        if (recipeRepository.count() > 0) {
            return recipeRepository.findAll();
        }

        // ---- Recipe 1 ----
        Recipe r1 = newRecipe("00000004-0000-0000-0000-000000000001", "Bò xào hành tây cà chua", 2, 25, Difficulty.EASY);
        r1.getIngredients().addAll(List.of(
                ri(r1, "00000005-0000-0000-0001-000000000001", "Thịt bò", 300, Unit.G),
                ri(r1, "00000005-0000-0000-0001-000000000002", "Hành tây", 2, Unit.PCS),
                ri(r1, "00000005-0000-0000-0001-000000000003", "Cà chua", 2, Unit.PCS),
                ri(r1, "00000005-0000-0000-0001-000000000004", "Dầu ăn", 20, Unit.ML),
                ri(r1, "00000005-0000-0000-0001-000000000005", "Nước mắm", 15, Unit.ML),
                ri(r1, "00000005-0000-0000-0001-000000000006", "Tiêu", 5, Unit.G)
        ));
        r1.getSteps().addAll(List.of(
                st(r1, "00000006-0000-0000-0001-000000000001", 1, "Sơ chế thịt bò, ướp với nước mắm và tiêu trong 15 phút."),
                st(r1, "00000006-0000-0000-0001-000000000002", 2, "Hành tây và cà chua rửa sạch, cắt múi cau."),
                st(r1, "00000006-0000-0000-0001-000000000003", 3, "Làm nóng chảo với dầu ăn, xào thịt bò chín tái rồi múc ra đĩa."),
                st(r1, "00000006-0000-0000-0001-000000000004", 4, "Xào hành tây, cà chua chín tới rồi cho thịt bò vào đảo nhanh, tắt bếp.")
        ));

        // ---- Recipe 2 ----
        Recipe r2 = newRecipe("00000004-0000-0000-0000-000000000002", "Trứng chiên hành tây", 2, 15, Difficulty.EASY);
        r2.getIngredients().addAll(List.of(
                ri(r2, "00000005-0000-0000-0002-000000000001", "Trứng gà", 3, Unit.PCS),
                ri(r2, "00000005-0000-0000-0002-000000000002", "Hành tây", 1, Unit.PCS),
                ri(r2, "00000005-0000-0000-0002-000000000003", "Nước mắm", 10, Unit.ML),
                ri(r2, "00000005-0000-0000-0002-000000000004", "Dầu ăn", 15, Unit.ML)
        ));
        r2.getSteps().addAll(List.of(
                st(r2, "00000006-0000-0000-0002-000000000001", 1, "Đập trứng gà vào tô, thêm nước mắm và đánh tan đều."),
                st(r2, "00000006-0000-0000-0002-000000000002", 2, "Hành tây băm nhỏ, trộn đều vào tô trứng."),
                st(r2, "00000006-0000-0000-0002-000000000003", 3, "Đun nóng dầu ăn trong chảo, đổ trứng vào chiên vàng đều hai mặt.")
        ));

        // ---- Recipe 3 ----
        Recipe r3 = newRecipe("00000004-0000-0000-0000-000000000003", "Canh cà chua trứng", 3, 20, Difficulty.EASY);
        r3.getIngredients().addAll(List.of(
                ri(r3, "00000005-0000-0000-0003-000000000001", "Cà chua", 3, Unit.PCS),
                ri(r3, "00000005-0000-0000-0003-000000000002", "Trứng gà", 2, Unit.PCS),
                ri(r3, "00000005-0000-0000-0003-000000000003", "Hành lá", 10, Unit.G),
                ri(r3, "00000005-0000-0000-0003-000000000004", "Nước mắm", 10, Unit.ML),
                ri(r3, "00000005-0000-0000-0003-000000000005", "Dầu ăn", 10, Unit.ML),
                ri(r3, "00000005-0000-0000-0003-000000000006", "Muối", 3, Unit.G)
        ));
        r3.getSteps().addAll(List.of(
                st(r3, "00000006-0000-0000-0003-000000000001", 1, "Cà chua cắt múi cau, hành lá cắt khúc, trứng đánh tan."),
                st(r3, "00000006-0000-0000-0003-000000000002", 2, "Phi thơm hành lá với dầu ăn, cho cà chua vào xào mềm."),
                st(r3, "00000006-0000-0000-0003-000000000003", 3, "Thêm 500ml nước, đun sôi rồi nêm nước mắm, muối."),
                st(r3, "00000006-0000-0000-0003-000000000004", 4, "Rót từ từ trứng vào nồi, khuấy nhẹ cho trứng tạo vân rồi tắt bếp.")
        ));

        // ---- Recipe 4 ----
        Recipe r4 = newRecipe("00000004-0000-0000-0000-000000000004", "Thịt heo kho trứng", 4, 60, Difficulty.MEDIUM);
        r4.getIngredients().addAll(List.of(
                ri(r4, "00000005-0000-0000-0004-000000000001", "Thịt heo", 500, Unit.G),
                ri(r4, "00000005-0000-0000-0004-000000000002", "Trứng gà", 4, Unit.PCS),
                ri(r4, "00000005-0000-0000-0004-000000000003", "Nước mắm", 30, Unit.ML),
                ri(r4, "00000005-0000-0000-0004-000000000004", "Đường", 20, Unit.G),
                ri(r4, "00000005-0000-0000-0004-000000000005", "Tiêu", 3, Unit.G),
                ri(r4, "00000005-0000-0000-0004-000000000006", "Hành tây", 1, Unit.PCS)
        ));
        r4.getSteps().addAll(List.of(
                st(r4, "00000006-0000-0000-0004-000000000001", 1, "Luộc trứng, bóc vỏ. Thịt heo cắt miếng vuông, ướp nước mắm, tiêu 15 phút."),
                st(r4, "00000006-0000-0000-0004-000000000002", 2, "Thắng đường làm nước màu, cho thịt vào đảo săn."),
                st(r4, "00000006-0000-0000-0004-000000000003", 3, "Đổ nước xâm xấp, thêm hành tây, hầm lửa nhỏ 30 phút."),
                st(r4, "00000006-0000-0000-0004-000000000004", 4, "Cho trứng vào kho thêm 15 phút đến khi nước sánh lại.")
        ));

        // ---- Recipe 5 ----
        Recipe r5 = newRecipe("00000004-0000-0000-0000-000000000005", "Rau muống xào tỏi", 2, 10, Difficulty.EASY);
        r5.getIngredients().addAll(List.of(
                ri(r5, "00000005-0000-0000-0005-000000000001", "Rau muống", 300, Unit.G),
                ri(r5, "00000005-0000-0000-0005-000000000002", "Tỏi", 15, Unit.G),
                ri(r5, "00000005-0000-0000-0005-000000000003", "Dầu ăn", 15, Unit.ML),
                ri(r5, "00000005-0000-0000-0005-000000000004", "Nước mắm", 5, Unit.ML)
        ));
        r5.getSteps().addAll(List.of(
                st(r5, "00000006-0000-0000-0005-000000000001", 1, "Rau muống nhặt, rửa sạch, cắt khúc; tỏi đập dập."),
                st(r5, "00000006-0000-0000-0005-000000000002", 2, "Phi thơm tỏi với dầu ăn trên lửa lớn."),
                st(r5, "00000006-0000-0000-0005-000000000003", 3, "Cho rau vào xào nhanh tay, nêm nước mắm rồi tắt bếp.")
        ));

        // ---- Recipe 6 ----
        Recipe r6 = newRecipe("00000004-0000-0000-0000-000000000006", "Gà xào gừng", 3, 30, Difficulty.MEDIUM);
        r6.getIngredients().addAll(List.of(
                ri(r6, "00000005-0000-0000-0006-000000000001", "Thịt gà", 400, Unit.G),
                ri(r6, "00000005-0000-0000-0006-000000000002", "Gừng", 20, Unit.G),
                ri(r6, "00000005-0000-0000-0006-000000000003", "Hành tây", 1, Unit.PCS),
                ri(r6, "00000005-0000-0000-0006-000000000004", "Nước mắm", 15, Unit.ML),
                ri(r6, "00000005-0000-0000-0006-000000000005", "Dầu ăn", 20, Unit.ML),
                ri(r6, "00000005-0000-0000-0006-000000000006", "Tiêu", 3, Unit.G)
        ));
        r6.getSteps().addAll(List.of(
                st(r6, "00000006-0000-0000-0006-000000000001", 1, "Thịt gà chặt miếng vừa ăn, ướp nước mắm, tiêu và gừng thái sợi."),
                st(r6, "00000006-0000-0000-0006-000000000002", 2, "Phi thơm gừng, cho gà vào xào lửa lớn đến khi săn lại."),
                st(r6, "00000006-0000-0000-0006-000000000003", 3, "Thêm hành tây, đậy nắp om 10 phút đến khi gà chín mềm.")
        ));

        List<Recipe> saved = recipeRepository.saveAll(List.of(r1, r2, r3, r4, r5, r6));
        log.info("Initialized {} sample recipes", saved.size());
        return saved;
    }

    private Recipe newRecipe(String id, String title, int servings, int minutes, Difficulty difficulty) {
        return Recipe.builder()
                .id(UUID.fromString(id))
                .title(title)
                .servings(servings)
                .readyInMinutes(minutes)
                .difficulty(difficulty)
                .custom(false)
                .ingredients(new ArrayList<>())
                .steps(new ArrayList<>())
                .build();
    }

    private RecipeIngredient ri(Recipe recipe, String id, String name, double amount, Unit unit) {
        return RecipeIngredient.builder()
                .id(UUID.fromString(id))
                .name(name)
                .amount(amount)
                .unit(unit)
                .recipe(recipe)
                .build();
    }

    private RecipeStep st(Recipe recipe, String id, int order, String text) {
        return RecipeStep.builder()
                .id(UUID.fromString(id))
                .stepOrder(order)
                .text(text)
                .recipe(recipe)
                .build();
    }

    // ------------------------------------------------------------------
    // Pantry items
    // ------------------------------------------------------------------

    private void initPantryItems(User admin, Map<String, Ingredient> ingredients) {
        if (pantryItemRepository.count() > 0) {
            return;
        }

        List<PantryItem> items = new ArrayList<>();
        //                  id                                        tên          SL       đơn vị    vị trí                        hạn (ngày từ hôm nay)
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000001", "Trứng gà",  10.0,   Unit.PCS, StorageLocation.FRIDGE,       14);
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000002", "Cà chua",   4.0,    Unit.PCS, StorageLocation.FRIDGE,       2);   // sắp hết hạn
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000003", "Hành tây",  1.0,    Unit.PCS, StorageLocation.PANTRY_SHELF, 5);
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000004", "Nước mắm",  500.0,  Unit.ML,  StorageLocation.PANTRY_SHELF, 180);
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000005", "Dầu ăn",    1000.0, Unit.ML,  StorageLocation.PANTRY_SHELF, 365);
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000006", "Thịt bò",   400.0,  Unit.G,   StorageLocation.FRIDGE,       3);   // sắp hết hạn
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000007", "Thịt gà",   600.0,  Unit.G,   StorageLocation.FRIDGE,       4);
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000008", "Rau muống", 250.0,  Unit.G,   StorageLocation.FRIDGE,       1);   // sắp hết hạn
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000009", "Tôm",       300.0,  Unit.G,   StorageLocation.FRIDGE,       -1);  // ĐÃ hết hạn
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000010", "Tỏi",       50.0,   Unit.G,   StorageLocation.PANTRY_SHELF, 30);
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000011", "Gừng",      40.0,   Unit.G,   StorageLocation.PANTRY_SHELF, 20);
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000012", "Tiêu",      100.0,  Unit.G,   StorageLocation.PANTRY_SHELF, 300);
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000013", "Muối",      500.0,  Unit.G,   StorageLocation.PANTRY_SHELF, 730);
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000014", "Đường",     1000.0, Unit.G,   StorageLocation.PANTRY_SHELF, 500);
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000015", "Gạo",       5.0,    Unit.KG,  StorageLocation.PANTRY_SHELF, 240);
        addPantry(items, admin, ingredients, "00000007-0000-0000-0000-000000000016", "Mì gói",    6.0,    Unit.PCS, StorageLocation.PANTRY_SHELF, 120);

        pantryItemRepository.saveAll(items);
        log.info("Initialized {} sample pantry items for admin", items.size());
    }

    private void addPantry(List<PantryItem> items, User user, Map<String, Ingredient> ingredients,
                           String id, String name, double qty, Unit unit, StorageLocation location, int expiresInDays) {
        Ingredient ingredient = ingredients.get(name);
        if (ingredient == null) return;
        items.add(PantryItem.builder()
                .id(UUID.fromString(id))
                .user(user)
                .ingredient(ingredient)
                .quantity(qty)
                .unit(unit)
                .location(location)
                .expiryDate(LocalDate.now().plusDays(expiresInDays))
                .build());
    }

    // ------------------------------------------------------------------
    // Meal plans
    // ------------------------------------------------------------------

    private void initMealPlans(User admin, List<Recipe> recipes) {
        if (mealPlanRepository.count() > 0 || recipes.isEmpty()) {
            return;
        }

        // Sắp xếp recipe theo title để thứ tự không phụ thuộc findAll() (khi DB đã có sẵn dữ liệu)
        List<Recipe> sorted = new ArrayList<>(recipes);
        sorted.sort(Comparator.comparing(Recipe::getId));

        LocalDate today = LocalDate.now();
        LocalDate thisWeek = today.with(DayOfWeek.MONDAY);
        LocalDate nextWeek = thisWeek.plusWeeks(1);

        MealPlan plan1 = buildWeekPlan(admin, MEAL_PLAN_THIS_WEEK, ENTRY_IDS_THIS_WEEK, thisWeek, sorted, today);
        MealPlan plan2 = buildWeekPlan(admin, MEAL_PLAN_NEXT_WEEK, ENTRY_IDS_NEXT_WEEK, nextWeek, sorted, today);

        mealPlanRepository.saveAll(List.of(plan1, plan2));
        log.info("Initialized sample meal plans for weeks {} and {}", thisWeek, nextWeek);
    }

    private MealPlan buildWeekPlan(User user, String planId, String[] entryIds, LocalDate weekStart,
                                   List<Recipe> recipes, LocalDate today) {
        MealPlan plan = MealPlan.builder()
                .id(UUID.fromString(planId))
                .user(user)
                .weekStart(weekStart)
                .entries(new ArrayList<>())
                .build();

        int idx = 0; // con trỏ vào mảng entryIds
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            boolean past = date.isBefore(today); // ngày đã qua -> đã nấu

            // Bữa tối mỗi ngày
            plan.getEntries().add(entry(entryIds[idx++], plan, date, MealType.DINNER, recipes.get(i % recipes.size()), 2, past));
            // Bữa trưa thứ 4, thứ 7
            if (i == 2 || i == 5) {
                plan.getEntries().add(entry(entryIds[idx++], plan, date, MealType.LUNCH, recipes.get((i + 2) % recipes.size()), 2, past));
            }
            // Bữa sáng Chủ nhật
            if (i == 6) {
                plan.getEntries().add(entry(entryIds[idx++], plan, date, MealType.BREAKFAST, recipes.get(1 % recipes.size()), 2, past));
            }
        }
        return plan;
    }

    private MealPlanEntry entry(String id, MealPlan plan, LocalDate date, MealType type, Recipe recipe, int servings, boolean cooked) {
        return MealPlanEntry.builder()
                .id(UUID.fromString(id))
                .date(date)
                .mealType(type)
                .recipe(recipe)
                .servings(servings)
                .cooked(cooked)
                .mealPlan(plan)
                .build();
    }
}