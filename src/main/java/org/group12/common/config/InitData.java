package org.group12.common.config;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.group12.common.enums.Unit;
import org.group12.feature.ingredient.entity.ExcludedIngredient;
import org.group12.feature.ingredient.entity.Ingredient;
import org.group12.feature.ingredient.entity.IngredientCategory;
import org.group12.feature.ingredient.repository.ExcludedIngredientRepository;
import org.group12.feature.ingredient.repository.IngredientCategoryRepository;
import org.group12.feature.ingredient.repository.IngredientRepository;

import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.util.stream.Stream;

@Slf4j
@Profile("dev")
@RequiredArgsConstructor
@Component
public class InitData implements CommandLineRunner {
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


    @Value("${app.password-default}")
    private String PASSWORD;
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String initData;

    @Override

    public void run(String @NonNull ... args) {
        if (!"create".equals(initData) && !"create-drop".equals(initData)) {
            log.info("Skipping data initialization because spring.jpa.hibernate.ddl-auto is not 'create' or 'create-drop'");

        } else {
            initRoles();
            initUsers();
            initCuisines();
            initIngredientCategories();
            initSyncStatuses();
            initIngredients();
            initExcludedIngredients();
            initRecipes();
            initPantryItems();
            initMealPlans();
            initSavedRecipes();
            initCollections();
            initFeedbacks();

            log.info("Data initialization completed successfully.");
        }

    }

    private void initSyncStatuses() {
        Map<String, String> statuses = new LinkedHashMap<>();
        statuses.put("PENDING", "Chờ đồng bộ");
        statuses.put("SYNCING", "Đang đồng bộ");
        statuses.put("SYNCED", "Đã đồng bộ");
        statuses.put("FAILED", "Đồng bộ thất bại");
        statuses.put("CONFLICT", "Xung đột dữ liệu");

        Set<String> existing = syncStatusRepository.findAllByCodeIn(statuses.keySet()).stream()
                .map(SyncStatus::getCode)
                .collect(Collectors.toSet());

        List<SyncStatus> missing = statuses.entrySet().stream()
                .filter(e -> !existing.contains(e.getKey()))
                .map(e -> SyncStatus.builder()
                        .id(appUtil.generateUUID())
                        .code(e.getKey())
                        .name(e.getValue())
                        .build())
                .toList();

        if (!missing.isEmpty()) {
            syncStatusRepository.saveAll(missing);
            log.info("Seeded {} sync statuses", missing.size());
        }
    }

    private void initIngredientCategories() {
        if (ingredientCategoryRepository.count() == 0) {
            List<String> rows = new ArrayList<>(List.of(
                    "VEGETABLE:Rau củ", "FRUIT:Trái cây", "MEAT:Thịt", "SEAFOOD:Hải sản", "EGG:Trứng",
                    "DAIRY:Sữa và chế phẩm từ sữa", "GRAIN:Gạo, bột, ngũ cốc", "LEGUME_NUT:Đậu và hạt",
                    "SPICE_HERB:Gia vị và rau thơm", "SAUCE_CONDIMENT:Nước chấm và sốt", "OIL_FAT:Dầu mỡ",
                    "CANNED_PROCESSED:Đồ hộp và thực phẩm chế biến sẵn", "BEVERAGE:Đồ uống", "OTHER:Khác"));
            // sortOrder: nhóm cuối danh sách ("Khác") = 1 để luôn nằm dưới cùng, các nhóm còn lại = 0
            List<IngredientCategory> categories = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                String[] p = rows.get(i).split(":");
                categories.add(IngredientCategory.builder()
                        .id(appUtil.generateUUID())
                        .code(p[0])
                        .name(p[1])
                        .sortOrder(i == rows.size() - 1 ? 1 : 0)
                        .build());
            }
            ingredientCategoryRepository.saveAll(categories);
        }
    }

    private void initIngredients() {
        if (ingredientRepository.count() > 0) {
            return;
        }

        Map<String, IngredientCategory> categories = ingredientCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(IngredientCategory::getCode, Function.identity()));

        Map<String, String[]> data = new LinkedHashMap<>();
        data.put("VEGETABLE", new String[]{"Cà chua:PCS", "Hành tây:PCS", "Cà rốt:PCS", "Khoai tây:G", "Rau muống:G",
                "Cải thảo:G", "Bông cải xanh:G", "Dưa leo:PCS", "Nấm rơm:G"});
        data.put("FRUIT", new String[]{"Chuối:PCS", "Chanh:PCS", "Cam:PCS", "Xoài:PCS", "Dứa:PCS"});
        data.put("MEAT", new String[]{"Thịt ba chỉ:G", "Thịt bò:G", "Ức gà:G", "Sườn heo:G", "Thịt heo xay:G"});
        data.put("SEAFOOD", new String[]{"Tôm:G", "Cá basa:G", "Mực:G", "Cá hồi:G"});
        data.put("EGG", new String[]{"Trứng gà:PCS", "Trứng vịt:PCS", "Trứng cút:PCS"});
        data.put("DAIRY", new String[]{"Sữa tươi:ML:8934000000011", "Bơ:G", "Phô mai:G", "Sữa chua:G"});
        data.put("GRAIN", new String[]{"Gạo:G", "Bún tươi:G", "Mì sợi:G", "Bột mì:G", "Bánh phở:G"});
        data.put("LEGUME_NUT", new String[]{"Đậu hũ:PCS", "Đậu phộng:G", "Đậu xanh:G", "Mè trắng:G"});
        data.put("SPICE_HERB", new String[]{"Tỏi:G", "Hành lá:G", "Sả:PCS", "Gừng:G", "Ớt:PCS", "Rau mùi:G",
                "Muối:TSP", "Đường:TSP", "Tiêu:TSP"});
        data.put("SAUCE_CONDIMENT", new String[]{"Nước mắm:ML:8934000000028", "Nước tương:ML", "Dầu hào:ML",
                "Tương ớt:ML:8934000000035"});
        data.put("OIL_FAT", new String[]{"Dầu ăn:ML", "Dầu olive:ML", "Mỡ heo:G"});
        data.put("CANNED_PROCESSED", new String[]{"Cá hộp:G", "Xúc xích:PCS", "Ngô hộp:G"});
        data.put("BEVERAGE", new String[]{"Nước dừa:ML", "Cà phê:G", "Trà:G"});
        data.put("OTHER", new String[]{"Men nở:G"});

        List<Ingredient> ingredients = new ArrayList<>();
        data.forEach((categoryCode, rows) -> {
            IngredientCategory category = categories.get(categoryCode);
            if (category == null) {
                log.warn("Skip seeding ingredients: category {} not found", categoryCode);
                return;
            }
            for (String row : rows) {
                String[] p = row.split(":");
                ingredients.add(Ingredient.builder()
                        .id(appUtil.generateUUID())
                        .name(p[0])
                        .defaultUnit(Unit.valueOf(p[1]))
                        .barcode(p.length > 2 ? p[2] : null)
                        .category(category)
                        .build());
            }
        });
        ingredientRepository.saveAll(ingredients);
    }

    /**
     * Vài nguyên liệu loại trừ cho tài khoản admin để test API.
     */
    private void initExcludedIngredients() {
        if (excludedIngredientRepository.count() > 0) {
            return;
        }
        userRepository.findByEmail("admin@gmail.com").ifPresent(admin ->
                excludedIngredientRepository.saveAll(
                        Stream.of("Tôm", "Đậu phộng", "Sữa tươi")
                                .map(name -> ExcludedIngredient.builder()
                                        .id(appUtil.generateUUID())
                                        .name(name)
                                        .user(admin)
                                        .build())
                                .toList()));
    }

    private void initUsers() {
        Faker fakeData = new Faker();
        int numberOfUsers = 100;
        Role role = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();
        if (userRepository.count() == 0) {

            User user = User.builder()
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .measurementSystem(MeasurementSystem.METRIC)
                    .id(appUtil.generateUUID())
                    .themeMode(org.group12.feature.auth.enums.ThemeMode.LIGHT)
                    .roles(Set.copyOf(roleRepository.findAll()))
                    .build();
            userRepository.save(user);

            List<User> users = new ArrayList<>();
            for (int i = 0; i < numberOfUsers; i++) {
                String email = fakeData.internet().emailAddress();
                String password = PASSWORD;
                String displayName = fakeData.name().fullName();
                MeasurementSystem measurementSystem = i % 2 == 0 ? MeasurementSystem.METRIC : MeasurementSystem.IMPERIAL;
                ThemeMode themeMode = i % 2 == 0 ? ThemeMode.LIGHT : ThemeMode.DARK;

                users.add(User.builder()
                        .id(appUtil.generateUUID())
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .displayName(displayName)
                        .measurementSystem(measurementSystem)
                        .themeMode(themeMode)
                        .roles(Set.of(role))
                        .build());

            }
            userRepository.saveAll(users);
        }
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            List<Role> roles = List.of(Role.builder().id(appUtil.generateUUID()).name(RoleName.ROLE_ADMIN).build(), Role.builder().id(appUtil.generateUUID()).name(RoleName.ROLE_USER).build());
            roleRepository.saveAll(roles);
        }
    }

    private void initCuisines() {
        if (cuisineRepository.count() == 0) {
            List<Cuisine> cuisines = Stream.of("VIETNAMESE:Vietnamese", "ASIAN:Asian", "WESTERN:Western", "ITALIAN:Italian", "OTHER:Other")
                    .map(s -> s.split(":"))
                    .map(p -> Cuisine.builder().id(appUtil.generateUUID()).code(p[0]).name(p[1]).build())
                    .toList();
            cuisineRepository.saveAll(cuisines);
        }
    }


    private static final List<String> DISHES = List.of(
            "Phở bò", "Bún chả", "Cơm tấm sườn", "Bánh xèo", "Gỏi cuốn", "Canh chua cá lóc",
            "Thịt kho tàu", "Bún bò Huế", "Mì Quảng", "Cá kho tộ", "Rau muống xào tỏi",
            "Gà nướng sả", "Bò lúc lắc", "Canh bí đao tôm", "Đậu hũ sốt cà chua",
            "Trứng chiên hành", "Chả giò", "Bún riêu cua", "Hủ tiếu Nam Vang", "Cháo gà");

    private static final List<String> STEPS = List.of(
            "Sơ chế và rửa sạch nguyên liệu.",
            "Ướp nguyên liệu với gia vị trong 15 phút.",
            "Phi thơm hành tỏi với dầu ăn.",
            "Cho nguyên liệu chính vào xào hoặc nướng đến khi săn lại.",
            "Thêm nước và nấu liu riu đến khi mềm.",
            "Nêm nếm lại cho vừa ăn.",
            "Bày ra đĩa và thưởng thức khi còn nóng.");

    private void initRecipes() {
        if (recipeRepository.count() > 0) {
            return;
        }

        List<Cuisine> cuisines = cuisineRepository.findAll();
        List<Ingredient> pool = ingredientRepository.findAll();
        List<User> owners = userRepository.findAll(PageRequest.of(0, 5)).getContent();
        if (cuisines.isEmpty() || pool.isEmpty()) {
            log.warn("Skip seeding recipes: cuisines or ingredients missing");
            return;
        }

        Faker faker = new Faker(new Random(42)); // seed cố định → dữ liệu lặp lại được
        Difficulty[] difficulties = Difficulty.values();
        int total = 60;
        int customCount = owners.isEmpty() ? 0 : 10;

        List<Recipe> recipes = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            boolean custom = i < customCount;
            String title = DISHES.get(i % DISHES.size())
                    + (i >= DISHES.size() ? " #" + (i / DISHES.size() + 1) : "");

            Recipe recipe = Recipe.builder()
                    .id(appUtil.generateUUID())
                    .title(title)
                    .imageUrl("https://picsum.photos/seed/recipe" + i + "/600/400")
                    .servings(faker.number().numberBetween(1, 8))
                    .readyInMinutes(faker.number().numberBetween(10, 120))
                    .difficulty(difficulties[faker.number().numberBetween(0, difficulties.length)])
                    .custom(custom)
                    .cuisine(cuisines.get(faker.number().numberBetween(0, cuisines.size())))
                    .build();

            if (custom) {
                recipe.setOwner(owners.get(i % owners.size()));
            } else {
//                recipe.setExternalId("seed-" + (100000 + i));
//                recipe.setSourceUrl("https://example.com/recipes/seed-" + (100000 + i));
            }

            List<Ingredient> shuffled = new ArrayList<>(pool);
            Collections.shuffle(shuffled, new Random(i));
            int ingredientCount = faker.number().numberBetween(4, 9);
            for (Ingredient ing : shuffled.subList(0, Math.min(ingredientCount, shuffled.size()))) {
                recipe.getIngredients().add(RecipeIngredient.builder()
                        .id(appUtil.generateUUID())
                        .recipe(recipe)
                        .name(ing.getName())
                        .amount((double) faker.number().numberBetween(1, 500))
                        .unit(ing.getDefaultUnit())
                        .build());
            }

            int stepCount = faker.number().numberBetween(3, STEPS.size() + 1);
            for (int s = 0; s < stepCount; s++) {
                recipe.getSteps().add(RecipeStep.builder()
                        .id(appUtil.generateUUID())
                        .recipe(recipe)
                        .stepOrder(s + 1)
                        .text(STEPS.get(s))
                        .timerSeconds(STEP_TIMERS.get(s))   // null nếu bước không cần hẹn giờ
                        .build());
            }

            recipes.add(recipe);
        }

        recipeRepository.saveAll(recipes);
        log.info("Seeded {} recipes ({} custom)", recipes.size(), customCount);
    }

    // cùng thứ tự với STEPS: bước 2 ướp 15 phút, bước 5 hầm 20 phút
    private static final List<Integer> STEP_TIMERS = Arrays.asList(
            null, 900, null, null, 1200, null, null);

    private void initPantryItems() {
        if (pantryItemRepository.count() > 0) {
            return;
        }
        User admin = userRepository.findByEmail("admin@gmail.com").orElse(null);
        List<Ingredient> ingredients = ingredientRepository.findAll();
        if (admin == null || ingredients.isEmpty()) {
            log.warn("Skip seeding pantry items: admin or ingredients missing");
            return;
        }

        SyncStatus synced = syncStatusRepository.findAllByCodeIn(Set.of("SYNCED")).stream()
                .findFirst().orElse(null);

        // bỏ các nguyên liệu admin đã loại trừ để dữ liệu test không mâu thuẫn
        Set<String> excluded = Set.of("Tôm", "Đậu phộng", "Sữa tươi");
        List<Ingredient> candidates = ingredients.stream()
                .filter(i -> !excluded.contains(i.getName()))
                .collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(candidates, new Random(42));   // seed cố định → lặp lại được
        Random random = new Random(42);
        StorageLocation[] locations = StorageLocation.values();
        LocalDate today = LocalDate.now();

        List<PantryItem> items = candidates.stream()
                .limit(15)
                .map(ing -> PantryItem.builder()
                        .id(appUtil.generateUUID())
                        .user(admin)
                        .ingredient(ing)
                        .quantity((random.nextDouble(500) + 1))
                        .unit(ing.getDefaultUnit())
                        .location(locations[random.nextInt(locations.length)])
                        .expiryDate(today.plusDays(random.nextInt(30) + 1))
                        .syncStatus(synced)
                        .build())
                .toList();

        pantryItemRepository.saveAll(items);
        log.info("Seeded {} pantry items for admin", items.size());
    }

    private void initMealPlans() {
        if (mealPlanRepository.count() > 0) {
            return;
        }
        User admin = userRepository.findByEmail("admin@gmail.com").orElse(null);
        List<Recipe> recipes = new ArrayList<>(recipeRepository.findAll());
        if (admin == null || recipes.isEmpty()) {
            log.warn("Skip seeding meal plans: admin or recipes missing");
            return;
        }

        // công thức custom của người khác không được dùng cho admin
        recipes.removeIf(r -> r.isCustom()
                && (r.getOwner() == null || !r.getOwner().getId().equals(admin.getId())));
        if (recipes.isEmpty()) {
            log.warn("Skip seeding meal plans: no usable recipes");
            return;
        }

        Collections.shuffle(recipes, new Random(42));   // seed cố định → lặp lại được
        Random random = new Random(42);
        LocalDate today = LocalDate.now();
        LocalDate thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        MealType[] meals = MealType.values();
        int cursor = 0;

        List<MealPlan> plans = new ArrayList<>();
        for (int week = 0; week < 2; week++) {               // tuần này + tuần sau
            MealPlan plan = MealPlan.builder()
                    .id(appUtil.generateUUID())
                    .weekStart(thisMonday.plusWeeks(week))
                    .user(admin)
                    .build();

            for (int day = 0; day < 7; day++) {
                LocalDate date = plan.getWeekStart().plusDays(day);
                for (MealType meal : meals) {
                    Recipe recipe = recipes.get(cursor++ % recipes.size());   // xoay vòng, ít trùng món
                    plan.getEntries().add(MealPlanEntry.builder()
                            .id(appUtil.generateUUID())
                            .mealPlan(plan)
                            .recipe(recipe)
                            .date(date)
                            .mealType(meal)
                            .servings(random.nextInt(4) + 1)
                            .cooked(date.isBefore(today))      // ngày đã qua = đã nấu
                            .build());
                }
            }
            plans.add(plan);
        }

        mealPlanRepository.saveAll(plans);
        log.info("Seeded {} meal plans ({} entries) for admin", plans.size(),
                plans.stream().mapToInt(p -> p.getEntries().size()).sum());
    }

    private static final List<String> NOTES = Arrays.asList(
            "Nêm ít muối hơn lần sau.", "Cả nhà đều thích.", "Nên ướp lâu hơn.",
            "Hợp để nấu cuối tuần.", "Giảm đường một chút.", null, null);

    private void initSavedRecipes() {
        if (savedRecipeRepository.count() > 0) {
            return;
        }
        User admin = userRepository.findByEmail("admin@gmail.com").orElse(null);
        List<Recipe> recipes = new ArrayList<>(recipeRepository.findAll());
        if (admin == null || recipes.isEmpty()) {
            log.warn("Skip seeding saved recipes: admin or recipes missing");
            return;
        }

        // công thức custom của người khác không được lưu vào sách của admin
        recipes.removeIf(r -> r.isCustom()
                && (r.getOwner() == null || !r.getOwner().getId().equals(admin.getId())));

        SyncStatus synced = syncStatusRepository.findAllByCodeIn(Set.of("SYNCED")).stream()
                .findFirst().orElse(null);

        Collections.shuffle(recipes, new Random(42));   // seed cố định → lặp lại được
        Random random = new Random(42);

        List<SavedRecipe> saved = recipes.stream()
                .limit(15)
                .map(recipe -> SavedRecipe.builder()
                        .id(appUtil.generateUUID())
                        .user(admin)
                        .recipe(recipe)
                        .rating(random.nextInt(5) + 1)                       // 1..5
                        .note(NOTES.get(random.nextInt(NOTES.size())))       // có thể null
                        .favorite(random.nextInt(100) < 30)                  // ~30% yêu thích
                        .syncStatus(synced)
                        .build())
                .toList();

        savedRecipeRepository.saveAll(saved);
        log.info("Seeded {} saved recipes for admin", saved.size());
    }
    private static final List<String> COLLECTION_NAMES = List.of(
            "Món yêu thích", "Nấu nhanh ngày thường", "Cuối tuần", "Món chay", "Tiệc gia đình");

    private void initCollections() {
        if (collectionRepository.count() > 0) {
            return;
        }
        User admin = userRepository.findByEmail("admin@gmail.com").orElse(null);
        if (admin == null) {
            log.warn("Skip seeding collections: admin missing");
            return;
        }
        List<SavedRecipe> savedRecipes = savedRecipeRepository.findAll().stream()
                .filter(s -> s.getUser().getId().equals(admin.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        if (savedRecipes.isEmpty()) {
            log.warn("Skip seeding collections: no saved recipes");
            return;
        }

        Collections.shuffle(savedRecipes, new Random(42));   // seed cố định → lặp lại được
        Random random = new Random(42);

        List<org.group12.feature.cookbook.entity.Collection> collections = new ArrayList<>();
        for (String name : COLLECTION_NAMES) {
            int size = Math.min(savedRecipes.size(), random.nextInt(4) + 2);   // 2..5 món
            Collections.shuffle(savedRecipes, new Random(random.nextLong()));

            collections.add(org.group12.feature.cookbook.entity.Collection.builder()
                    .id(appUtil.generateUUID())
                    .name(name)
                    .user(admin)
                    .savedRecipes(new HashSet<>(savedRecipes.subList(0, size)))
                    .build());
        }

        collectionRepository.saveAll(collections);
        log.info("Seeded {} collections for admin", collections.size());
    }

    private static final List<String> FEEDBACK_MESSAGES = List.of(
            "Ứng dụng bị lỗi khi quét ảnh nguyên liệu.",
            "Gợi ý thay thế nguyên liệu rất hữu ích.",
            "Mong muốn thêm chế độ tối cho màn hình công thức.",
            "Không thể lưu thực đơn tuần, báo lỗi 500.",
            "Giao diện tủ nguyên liệu dễ dùng, cảm ơn nhóm.",
            "Thông báo hết hạn đến chậm vài giờ.",
            "Đề xuất thêm bộ lọc theo thời gian nấu.",
            "Ảnh món ăn tải rất lâu trên mạng 4G.",
            "Nút yêu thích đôi khi không phản hồi.",
            "Công thức AI điều chỉnh khá chính xác.");

    private static final List<String> APP_VERSIONS = List.of("1.0.0", "1.0.1", "1.1.0", "1.2.0");
    private static final List<String> DEVICES = List.of(
            "Pixel 7 / Android 14", "Galaxy S23 / Android 14",
            "iPhone 14 / iOS 17", "iPhone 12 / iOS 16", "Redmi Note 12 / Android 13");

    private void initFeedbacks() {
        if (feedbackRepository.count() > 0) {
            return;
        }
        List<User> users = userRepository.findAll(PageRequest.of(0, 20)).getContent();
        if (users.isEmpty()) {
            log.warn("Skip seeding feedbacks: no users");
            return;
        }

        Random random = new Random(42);   // seed cố định → lặp lại được
        FeedbackType[] types = FeedbackType.values();

        List<Feedback> feedbacks = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            String message = FEEDBACK_MESSAGES.get(i % FEEDBACK_MESSAGES.size())
                    + (i >= FEEDBACK_MESSAGES.size() ? " (#" + (i / FEEDBACK_MESSAGES.size() + 1) + ")" : "");

            feedbacks.add(Feedback.builder()
                    .id(appUtil.generateUUID())
                    .type(types[random.nextInt(types.length)])
                    .message(message)
                    .appVersion(APP_VERSIONS.get(random.nextInt(APP_VERSIONS.size())))
                    .deviceInfo(DEVICES.get(random.nextInt(DEVICES.size())))
                    .user(users.get(random.nextInt(users.size())))
                    .build());
        }

        feedbackRepository.saveAll(feedbacks);
        log.info("Seeded {} feedbacks", feedbacks.size());
    }
}