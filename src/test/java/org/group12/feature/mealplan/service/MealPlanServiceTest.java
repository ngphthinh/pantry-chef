package org.group12.feature.mealplan.service;

import org.group12.common.enums.Unit;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.entity.User;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.ingredient.entity.Ingredient;
import org.group12.feature.mealplan.dto.request.MealPlanCreateRequest;
import org.group12.feature.mealplan.dto.request.MealPlanEntryCreateRequest;
import org.group12.feature.mealplan.dto.response.MealPlanResponse;
import org.group12.feature.mealplan.entity.MealPlan;
import org.group12.feature.mealplan.entity.MealPlanEntry;
import org.group12.feature.mealplan.enums.MealType;
import org.group12.feature.mealplan.exception.MealPlanEntryDateInvalidException;
import org.group12.feature.mealplan.exception.MealPlanWeekDuplicateException;
import org.group12.feature.mealplan.mapper.MealPlanEntryMapper;
import org.group12.feature.mealplan.mapper.MealPlanMapper;
import org.group12.feature.mealplan.repository.MealPlanEntryRepository;
import org.group12.feature.mealplan.repository.MealPlanRepository;
import org.group12.feature.pantry.entity.PantryItem;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.entity.RecipeIngredient;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.group12.feature.shopping.dto.response.ShoppingItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;
    @Mock
    private MealPlanEntryRepository mealPlanEntryRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private PantryItemRepository pantryItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MealPlanMapper mealPlanMapper;
    @Mock
    private MealPlanEntryMapper mealPlanEntryMapper;
    @Mock
    private AppUtil appUtil;

    @InjectMocks
    private MealPlanService mealPlanService;

    private UUID userId;
    private UUID mealPlanId;
    private UUID recipeId;
    private User user;
    private Recipe recipe;
    private MealPlan mealPlan;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mealPlanId = UUID.randomUUID();
        recipeId = UUID.randomUUID();

        user = User.builder().id(userId).email("user@test.com").build();

        recipe = Recipe.builder()
                .id(recipeId)
                .title("Trứng chiên hành tây")
                .servings(2)
                .ingredients(new ArrayList<>())
                .build();

        recipe.getIngredients().add(RecipeIngredient.builder()
                .id(UUID.randomUUID())
                .name("Trứng gà")
                .amount(4.0)
                .unit(Unit.PCS)
                .recipe(recipe)
                .build());

        recipe.getIngredients().add(RecipeIngredient.builder()
                .id(UUID.randomUUID())
                .name("Hành tây")
                .amount(2.0)
                .unit(Unit.PCS)
                .recipe(recipe)
                .build());

        mealPlan = MealPlan.builder()
                .id(mealPlanId)
                .user(user)
                .weekStart(LocalDate.of(2026, 10, 19))
                .entries(new ArrayList<>())
                .build();

        mealPlan.getEntries().add(MealPlanEntry.builder()
                .id(UUID.randomUUID())
                .date(LocalDate.of(2026, 10, 19))
                .mealType(MealType.DINNER)
                .recipe(recipe)
                .servings(2)
                .cooked(false)
                .mealPlan(mealPlan)
                .build());
    }

    @Test
    void create_DuplicateWeek_ThrowsException() {
        LocalDate weekStart = LocalDate.of(2026, 10, 19);
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
        when(mealPlanRepository.existsByUserIdAndWeekStart(userId, weekStart)).thenReturn(true);

        MealPlanCreateRequest request = MealPlanCreateRequest.builder().weekStart(weekStart).build();

        assertThrows(MealPlanWeekDuplicateException.class, () -> mealPlanService.create(request));
    }

    @Test
    void create_InvalidEntryDate_ThrowsException() {
        LocalDate weekStart = LocalDate.of(2026, 10, 19);
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
        when(mealPlanRepository.existsByUserIdAndWeekStart(userId, weekStart)).thenReturn(false);
        when(mealPlanMapper.toMealPlan(any())).thenReturn(mealPlan);
        when(appUtil.generateUUID()).thenReturn(mealPlanId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);

        MealPlanEntryCreateRequest invalidEntry = MealPlanEntryCreateRequest.builder()
                .date(LocalDate.of(2026, 10, 26)) // 7 days later (outside week [19..25])
                .mealType(MealType.DINNER)
                .servings(2)
                .build();

        MealPlanCreateRequest request = MealPlanCreateRequest.builder()
                .weekStart(weekStart)
                .entries(List.of(invalidEntry))
                .build();

        assertThrows(MealPlanEntryDateInvalidException.class, () -> mealPlanService.create(request));
    }

    @Test
    void create_Success() {
        LocalDate weekStart = LocalDate.of(2026, 10, 19);
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
        when(mealPlanRepository.existsByUserIdAndWeekStart(userId, weekStart)).thenReturn(false);
        when(mealPlanMapper.toEntity(any())).thenReturn(mealPlan);
        when(appUtil.generateUUID()).thenReturn(mealPlanId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(mealPlanRepository.save(any(MealPlan.class))).thenReturn(mealPlan);
        when(mealPlanMapper.toResponse(mealPlan)).thenReturn(MealPlanResponse.builder().id(mealPlanId).weekStart(weekStart).build());

        MealPlanCreateRequest request = MealPlanCreateRequest.builder().weekStart(weekStart).build();
        MealPlanResponse response = mealPlanService.create(request);

        assertNotNull(response);
        assertEquals(mealPlanId, response.getId());
    }

    @Test
    void copy_Success() {
        LocalDate toWeekStart = LocalDate.of(2026, 10, 26);
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
        when(mealPlanRepository.findByIdAndUserId(mealPlanId, userId)).thenReturn(Optional.of(mealPlan));
        when(mealPlanRepository.existsByUserIdAndWeekStart(userId, toWeekStart)).thenReturn(false);
        when(appUtil.generateUUID()).thenReturn(UUID.randomUUID());
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(mealPlanRepository.save(any(MealPlan.class))).thenReturn(mealPlan);
        when(mealPlanMapper.toResponse(any())).thenReturn(MealPlanResponse.builder().weekStart(toWeekStart).build());

        MealPlanResponse response = mealPlanService.copy(mealPlanId, toWeekStart);

        assertNotNull(response);
        assertEquals(toWeekStart, response.getWeekStart());
        verify(mealPlanRepository, times(1)).save(any(MealPlan.class));
    }

    @Test
    void deleteEntry_Success() {
        UUID entryId = mealPlan.getEntries().get(0).getId();
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
        when(mealPlanRepository.findByIdAndUserId(mealPlanId, userId)).thenReturn(Optional.of(mealPlan));
        when(mealPlanEntryRepository.findByIdAndMealPlanId(entryId, mealPlanId))
                .thenReturn(Optional.of(mealPlan.getEntries().get(0)));

        mealPlanService.deleteEntry(mealPlanId, entryId);

        assertTrue(mealPlan.getEntries().isEmpty());
        verify(mealPlanEntryRepository, times(1)).delete(any(MealPlanEntry.class));
    }

    @Test
    void getShoppingListPreview_CalculatesCorrectly() {
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
        when(mealPlanRepository.findByIdAndUserId(mealPlanId, userId)).thenReturn(Optional.of(mealPlan));
        when(appUtil.generateUUID()).thenReturn(UUID.randomUUID());

        // In pantry: user already has 2 Trứng gà, but 0 Hành tây
        PantryItem eggPantry = PantryItem.builder()
                .ingredient(Ingredient.builder().name("Trứng gà").build())
                .quantity(2.0)
                .unit(Unit.PCS)
                .build();

        when(pantryItemRepository.findByUserId(userId)).thenReturn(List.of(eggPantry));

        List<ShoppingItemResponse> shoppingList = mealPlanService.getShoppingListPreview(mealPlanId);

        // Recipe needs: 4 eggs, 2 onions.
        // Pantry has: 2 eggs, 0 onions.
        // Shopping list should have: 2 eggs (4 - 2), 2 onions (2 - 0).
        assertEquals(2, shoppingList.size());

        ShoppingItemResponse eggItem = shoppingList.stream()
                .filter(i -> i.getName().equalsIgnoreCase("Trứng gà"))
                .findFirst().orElseThrow();
        assertEquals(2.0, eggItem.getQuantity());
        assertNotNull(eggItem.getId());

        ShoppingItemResponse onionItem = shoppingList.stream()
                .filter(i -> i.getName().equalsIgnoreCase("Hành tây"))
                .findFirst().orElseThrow();
        assertEquals(2.0, onionItem.getQuantity());
        assertNotNull(onionItem.getId());
    }
}
