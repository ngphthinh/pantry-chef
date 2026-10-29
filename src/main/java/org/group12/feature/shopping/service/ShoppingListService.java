package org.group12.feature.shopping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.dto.response.CursorResponse;
import org.group12.common.enums.Unit;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.ingredient.entity.Ingredient;
import org.group12.feature.ingredient.repository.IngredientRepository;
import org.group12.feature.mealplan.service.MealPlanService;
import org.group12.feature.pantry.dto.request.PantryItemBatchCreateRequest;
import org.group12.feature.pantry.dto.request.PantryItemCreateRequest;
import org.group12.feature.pantry.dto.response.PantryItemResponse;
import org.group12.feature.pantry.service.PantryItemService;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.group12.feature.shopping.dto.request.ShoppingListCreateRequest;
import org.group12.feature.shopping.dto.request.ShoppingListUpdateRequest;
import org.group12.feature.shopping.dto.response.ShoppingItemResponse;
import org.group12.feature.shopping.dto.response.ShoppingListDetailResponse;
import org.group12.feature.shopping.dto.response.ShoppingListResponse;
import org.group12.feature.shopping.entity.ShoppingItem;
import org.group12.feature.shopping.entity.ShoppingList;
import org.group12.feature.shopping.enums.ShoppingItemStatus;
import org.group12.feature.shopping.exception.NothingToBuyException;
import org.group12.feature.shopping.exception.ShoppingListNotFoundException;
import org.group12.feature.shopping.mapper.ShoppingItemMapper;
import org.group12.feature.shopping.mapper.ShoppingListMapper;
import org.group12.feature.shopping.repository.ShoppingItemRepository;
import org.group12.feature.shopping.repository.ShoppingListRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_NAME_LENGTH = 255;
    private static final String MEAL_PLAN_LIST_NAME = "Thực đơn tuần %s";
    private static final DateTimeFormatter WEEK_START_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Mục không ghi số lượng hoặc đơn vị vẫn phải vào được tủ (pantry bắt buộc có cả hai)
    private static final double DEFAULT_QUANTITY = 1.0;
    private static final Unit DEFAULT_UNIT = Unit.PCS;

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final MealPlanService mealPlanService;
    private final PantryItemService pantryItemService;
    private final ShoppingListMapper shoppingListMapper;
    private final ShoppingItemMapper shoppingItemMapper;
    private final AppUtil appUtil;

    /** Một mục sắp thêm vào danh sách mới, chưa có id. */
    private record DraftItem(String name, Double quantity, Unit unit) {
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ShoppingListResponse create(ShoppingListCreateRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();

        ShoppingList list = shoppingListMapper.toShoppingList(request);
        list.setId(appUtil.generateUUID());
        list.setName(list.getName().trim());
        list.setUser(userRepository.getReferenceById(userId));

        list = shoppingListRepository.save(list);
        log.info("User {} created shopping list {}", userId, list.getId());
        return shoppingListMapper.toShoppingListResponse(list);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public CursorResponse<ShoppingListResponse> getMyLists(UUID cursor, int size) {
        UUID userId = appUtil.userIdFromAuthentication();
        int limit = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(0, limit + 1);

        List<ShoppingList> lists = cursor == null
                ? shoppingListRepository.findByUserIdOrderByIdDesc(userId, pageable)
                : shoppingListRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, cursor, pageable);

        return appUtil.buildCursorResponse(lists, limit, ShoppingList::getId, shoppingListMapper::toShoppingListResponse);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public ShoppingListDetailResponse getDetail(UUID id) {
        return buildDetail(getOwnedList(id));
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ShoppingListResponse update(UUID id, ShoppingListUpdateRequest request) {
        ShoppingList list = getOwnedList(id);
        shoppingListMapper.updateShoppingListEntity(request, list);   // dirty checking tự ghi, không cần save()
        list.setName(list.getName().trim());
        return shoppingListMapper.toShoppingListResponse(list);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void delete(UUID id) {
        shoppingListRepository.delete(getOwnedList(id));   // @SQLDelete -> xóa mềm, cascade xuống các mục
    }

    /**
     * Tạo danh sách mua từ thực đơn: chỉ gồm phần còn thiếu sau khi đã trừ nguyên liệu trong tủ.
     * Cách tính dùng chung với phần xem trước của thực đơn ({@code MealPlanService#getShoppingListPreview}).
     */
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ShoppingListDetailResponse createFromMealPlan(UUID mealPlanId) {
        String weekStart = mealPlanService.getById(mealPlanId).getWeekStart().format(WEEK_START_FORMAT);
        List<DraftItem> drafts = mealPlanService.getShoppingListPreview(mealPlanId).stream()
                .map(item -> new DraftItem(item.getName(), item.getQuantity(), item.getUnit()))
                .toList();
        return createListWithItems(MEAL_PLAN_LIST_NAME.formatted(weekStart), drafts);
    }

    /**
     * Tạo danh sách mua từ một công thức, lấy đủ nguyên liệu của công thức (không trừ đồ trong tủ).
     * Công thức tự tạo của người khác trả {@link RecipeNotFoundException}.
     */
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ShoppingListDetailResponse createFromRecipe(UUID recipeId) {
        UUID userId = appUtil.userIdFromAuthentication();
        Recipe recipe = recipeRepository.findAccessibleDetailById(recipeId, userId)
                .orElseThrow(RecipeNotFoundException::new);

        List<DraftItem> drafts = recipe.getIngredients().stream()
                .filter(ingredient -> ingredient.getName() != null && !ingredient.getName().isBlank())
                .map(ingredient -> {
                    boolean hasAmount = ingredient.getAmount() != null && ingredient.getAmount() > 0;
                    return new DraftItem(
                            ingredient.getName(),
                            hasAmount ? ingredient.getAmount() : null,
                            hasAmount ? ingredient.getUnit() : null);
                })
                .toList();
        return createListWithItems(recipe.getTitle(), drafts);
    }

    /**
     * Chuyển các mục đã mua vào tủ qua {@link PantryItemService}. Mục được ghép với nguyên liệu theo tên
     * (không phân biệt hoa thường); mục không có trong danh mục nguyên liệu thì giữ nguyên trong danh sách.
     * Các mục đã chuyển bị xóa mềm khỏi danh sách. Trả về các mục vừa thêm vào tủ.
     */
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public List<PantryItemResponse> moveBoughtToPantry(UUID id) {
        ShoppingList list = getOwnedList(id);
        List<ShoppingItem> bought = shoppingItemRepository
                .findByShoppingListIdAndStatusOrderByIdAsc(list.getId(), ShoppingItemStatus.BOUGHT);

        Map<String, Ingredient> ingredients = findIngredientsByName(bought);
        List<ShoppingItem> movable = bought.stream()
                .filter(item -> ingredients.containsKey(normalizeName(item.getName())))
                .toList();
        if (movable.isEmpty()) {
            return List.of();
        }

        List<PantryItemCreateRequest> requests = movable.stream()
                .map(item -> toPantryRequest(item, ingredients.get(normalizeName(item.getName()))))
                .toList();
        List<PantryItemResponse> created = pantryItemService
                .createBatch(PantryItemBatchCreateRequest.builder().items(requests).build());

        shoppingItemRepository.deleteAll(movable);   // @SQLDelete -> xóa mềm
        log.info("Moved {} of {} bought items of shopping list {} to pantry", movable.size(), bought.size(), id);
        return created;
    }

    /**
     * Lấy danh sách thuộc người dùng hiện tại. Danh sách của người khác hoặc không tồn tại đều
     * trả {@link ShoppingListNotFoundException} để không lộ sự tồn tại của bản ghi.
     * Dùng chung với {@code ShoppingItemService} (cùng feature).
     */
    @Transactional(readOnly = true)
    public ShoppingList getOwnedList(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        return shoppingListRepository.findByIdAndUserId(id, userId)
                .orElseThrow(ShoppingListNotFoundException::new);
    }

    private ShoppingListDetailResponse createListWithItems(String name, List<DraftItem> drafts) {
        if (drafts.isEmpty()) {
            throw new NothingToBuyException();
        }
        UUID userId = appUtil.userIdFromAuthentication();

        ShoppingList list = ShoppingList.builder()
                .id(appUtil.generateUUID())
                .name(truncate(name.trim()))
                .user(userRepository.getReferenceById(userId))
                .build();
        ShoppingList saved = shoppingListRepository.save(list);

        List<ShoppingItem> items = drafts.stream()
                .map(draft -> ShoppingItem.builder()
                        .id(appUtil.generateUUID())
                        .name(truncate(draft.name().trim()))
                        .quantity(draft.quantity())
                        .unit(draft.unit())
                        .status(ShoppingItemStatus.PENDING)
                        .shoppingList(saved)
                        .build())
                .toList();
        shoppingItemRepository.saveAll(items);

        log.info("User {} created shopping list {} with {} items", userId, saved.getId(), items.size());
        return buildDetail(saved);
    }

    /** Nạp một lần các nguyên liệu khớp tên với các mục; khoá là tên đã chuẩn hoá. */
    private Map<String, Ingredient> findIngredientsByName(List<ShoppingItem> items) {
        Set<String> names = items.stream().map(item -> normalizeName(item.getName())).collect(Collectors.toSet());
        if (names.isEmpty()) {
            return Map.of();
        }
        Map<String, Ingredient> byName = new HashMap<>();
        for (Ingredient ingredient : ingredientRepository.findByNormalizedNameIn(names)) {
            byName.putIfAbsent(normalizeName(ingredient.getName()), ingredient);   // trùng tên: lấy bản có id nhỏ nhất
        }
        return byName;
    }

    private PantryItemCreateRequest toPantryRequest(ShoppingItem item, Ingredient ingredient) {
        Unit fallbackUnit = ingredient.getDefaultUnit() != null ? ingredient.getDefaultUnit() : DEFAULT_UNIT;
        return PantryItemCreateRequest.builder()
                .ingredientId(ingredient.getId())
                .quantity(item.getQuantity() != null ? item.getQuantity() : DEFAULT_QUANTITY)
                .unit(item.getUnit() != null ? item.getUnit() : fallbackUnit)
                .build();
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private String truncate(String value) {
        return value.length() > MAX_NAME_LENGTH ? value.substring(0, MAX_NAME_LENGTH) : value;
    }

    private ShoppingListDetailResponse buildDetail(ShoppingList list) {
        List<ShoppingItemResponse> items = shoppingItemRepository.findByShoppingListIdOrderByIdAsc(list.getId())
                .stream()
                .map(shoppingItemMapper::toShoppingItemResponse)
                .toList();

        ShoppingListDetailResponse detail = shoppingListMapper.toShoppingListDetailResponse(list);
        detail.setItems(items);
        return detail;
    }
}
