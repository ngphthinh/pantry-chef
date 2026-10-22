package org.group12.feature.mealplan.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.enums.Unit;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.mealplan.dto.request.MealPlanCreateRequest;
import org.group12.feature.mealplan.dto.request.MealPlanEntryCreateRequest;
import org.group12.feature.mealplan.dto.request.MealPlanEntryUpdateRequest;
import org.group12.feature.mealplan.dto.response.MealPlanEntryResponse;
import org.group12.feature.mealplan.dto.response.MealPlanResponse;
import org.group12.feature.mealplan.entity.MealPlan;
import org.group12.feature.mealplan.entity.MealPlanEntry;
import org.group12.feature.mealplan.exception.MealPlanEntryNotFoundException;
import org.group12.feature.mealplan.exception.MealPlanNotFoundException;
import org.group12.feature.mealplan.exception.MealPlanWeekDuplicateException;
import org.group12.feature.mealplan.mapper.MealPlanEntryMapper;
import org.group12.feature.mealplan.mapper.MealPlanMapper;
import org.group12.feature.mealplan.repository.MealPlanEntryRepository;
import org.group12.feature.mealplan.repository.MealPlanRepository;
import org.group12.feature.pantry.entity.PantryItem;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.group12.feature.recipe.entity.Recipe;
import org.group12.feature.recipe.entity.RecipeIngredient;
import org.group12.feature.recipe.exception.RecipeNotFoundException;
import org.group12.feature.recipe.repository.RecipeRepository;
import org.group12.feature.shopping.dto.response.ShoppingItemResponse;
import org.group12.feature.shopping.enums.ShoppingItemStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@EnableWebSecurity
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealPlanEntryRepository mealPlanEntryRepository;
    private final RecipeRepository recipeRepository;
    private final PantryItemRepository pantryItemRepository;
    private final UserRepository userRepository;
    private final MealPlanMapper mealPlanMapper;
    private final MealPlanEntryMapper mealPlanEntryMapper;
    private final AppUtil appUtil;

    @Transactional(readOnly = true)
    public MealPlanResponse getByWeekStart(LocalDate weekStart) {
        UUID userId = appUtil.userIdFromAuthentication();
        return mealPlanRepository.findByUserIdAndWeekStart(userId, weekStart)
                .map(mealPlanMapper::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public MealPlanResponse getById(UUID id) {
        MealPlan mealPlan = findOwnedMealPlan(id);
        return mealPlanMapper.toResponse(mealPlan);
    }

    @Transactional
    public MealPlanResponse create(MealPlanCreateRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();

        if (mealPlanRepository.existsByUserIdAndWeekStart(userId, request.getWeekStart())) {
            throw new MealPlanWeekDuplicateException();
        }

        MealPlan mealPlan = mealPlanMapper.toEntity(request);
        mealPlan.setId(appUtil.generateUUID());
        mealPlan.setUser(userRepository.getReferenceById(userId));

        if (request.getEntries() != null && !request.getEntries().isEmpty()) {
            for (MealPlanEntryCreateRequest entryReq : request.getEntries()) {
                Recipe recipe = recipeRepository.findById(entryReq.getRecipeId())
                        .orElseThrow(RecipeNotFoundException::new);

                MealPlanEntry entry = mealPlanEntryMapper.toEntity(entryReq);
                entry.setId(appUtil.generateUUID());
                entry.setMealPlan(mealPlan);
                entry.setRecipe(recipe);
                entry.setCooked(false);
                mealPlan.getEntries().add(entry);
            }
        }

        mealPlan = mealPlanRepository.save(mealPlan);
        log.info("User {} created meal plan {} for week {}", userId, mealPlan.getId(), mealPlan.getWeekStart());
        return mealPlanMapper.toResponse(mealPlan);
    }

    @Transactional
    public void delete(UUID id) {
        MealPlan mealPlan = findOwnedMealPlan(id);
        mealPlanRepository.delete(mealPlan);
        log.info("User deleted meal plan {}", id);
    }

    @Transactional
    public MealPlanEntryResponse addEntry(UUID mealPlanId, MealPlanEntryCreateRequest request) {
        MealPlan mealPlan = findOwnedMealPlan(mealPlanId);

        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(RecipeNotFoundException::new);

        MealPlanEntry entry = mealPlanEntryMapper.toEntity(request);
        entry.setId(appUtil.generateUUID());
        entry.setMealPlan(mealPlan);
        entry.setRecipe(recipe);
        entry.setCooked(false);

        entry = mealPlanEntryRepository.save(entry);
        log.info("Added entry {} to meal plan {}", entry.getId(), mealPlanId);
        return mealPlanEntryMapper.toResponse(entry);
    }

    @Transactional
    public MealPlanEntryResponse updateEntry(UUID mealPlanId, UUID entryId, MealPlanEntryUpdateRequest request) {
        findOwnedMealPlan(mealPlanId);

        MealPlanEntry entry = mealPlanEntryRepository.findByIdAndMealPlanId(entryId, mealPlanId)
                .orElseThrow(MealPlanEntryNotFoundException::new);

        mealPlanEntryMapper.updateEntity(request, entry);
        log.info("Updated entry {} in meal plan {}", entryId, mealPlanId);
        return mealPlanEntryMapper.toResponse(entry);
    }

    @Transactional
    public void deleteEntry(UUID mealPlanId, UUID entryId) {
        findOwnedMealPlan(mealPlanId);

        MealPlanEntry entry = mealPlanEntryRepository.findByIdAndMealPlanId(entryId, mealPlanId)
                .orElseThrow(MealPlanEntryNotFoundException::new);

        mealPlanEntryRepository.delete(entry);
        log.info("Deleted entry {} from meal plan {}", entryId, mealPlanId);
    }

    @Transactional
    public MealPlanResponse copy(UUID id, LocalDate toWeekStart) {
        UUID userId = appUtil.userIdFromAuthentication();
        MealPlan sourcePlan = findOwnedMealPlan(id);

        if (mealPlanRepository.existsByUserIdAndWeekStart(userId, toWeekStart)) {
            throw new MealPlanWeekDuplicateException();
        }

        MealPlan newMealPlan = MealPlan.builder()
                .id(appUtil.generateUUID())
                .weekStart(toWeekStart)
                .user(userRepository.getReferenceById(userId))
                .build();

        long daysDiff = ChronoUnit.DAYS.between(sourcePlan.getWeekStart(), toWeekStart);

        if (sourcePlan.getEntries() != null) {
            for (MealPlanEntry oldEntry : sourcePlan.getEntries()) {
                MealPlanEntry newEntry = MealPlanEntry.builder()
                        .id(appUtil.generateUUID())
                        .date(oldEntry.getDate().plusDays(daysDiff))
                        .mealType(oldEntry.getMealType())
                        .recipe(oldEntry.getRecipe())
                        .servings(oldEntry.getServings())
                        .cooked(false)
                        .mealPlan(newMealPlan)
                        .build();
                newMealPlan.getEntries().add(newEntry);
            }
        }

        MealPlan savedPlan = mealPlanRepository.save(newMealPlan);
        log.info("Copied meal plan {} to new week {} with id {}", id, toWeekStart, savedPlan.getId());
        return mealPlanMapper.toResponse(savedPlan);
    }

    @Transactional(readOnly = true)
    public List<ShoppingItemResponse> getShoppingListPreview(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        MealPlan mealPlan = findOwnedMealPlan(id);

        @Getter
        @Setter
        @AllArgsConstructor
        class NeededItem {
            String displayName;
            double amount;
            Unit unit;
        }

        Map<String, NeededItem> neededMap = new LinkedHashMap<>();

        if (mealPlan.getEntries() != null) {
            for (MealPlanEntry entry : mealPlan.getEntries()) {
                Recipe recipe = entry.getRecipe();
                if (recipe == null || recipe.getIngredients() == null) {
                    continue;
                }

                int entryServings = entry.getServings() != null && entry.getServings() > 0
                        ? entry.getServings()
                        : (recipe.getServings() != null && recipe.getServings() > 0 ? recipe.getServings() : 1);
                int recipeServings = recipe.getServings() != null && recipe.getServings() > 0
                        ? recipe.getServings()
                        : 1;

                double ratio = (double) entryServings / recipeServings;

                for (RecipeIngredient ri : recipe.getIngredients()) {
                    if (ri.getName() == null || ri.getName().isBlank()) {
                        continue;
                    }
                    String normKey = ri.getName().trim().toLowerCase();
                    double amount = (ri.getAmount() != null ? ri.getAmount() : 1.0) * ratio;

                    if (neededMap.containsKey(normKey)) {
                        NeededItem existing = neededMap.get(normKey);
                        existing.setAmount(existing.getAmount() + amount);
                    } else {
                        neededMap.put(normKey, new NeededItem(ri.getName(), amount, ri.getUnit()));
                    }
                }
            }
        }

        List<PantryItem> pantryItems = pantryItemRepository.findByUserId(userId);
        for (PantryItem pi : pantryItems) {
            if (pi.getIngredient() == null || pi.getIngredient().getName() == null) {
                continue;
            }
            String pantryKey = pi.getIngredient().getName().trim().toLowerCase();
            if (neededMap.containsKey(pantryKey)) {
                NeededItem item = neededMap.get(pantryKey);
                double remaining = item.getAmount() - (pi.getQuantity() != null ? pi.getQuantity() : 0.0);
                item.setAmount(remaining);
            }
        }

        List<ShoppingItemResponse> shoppingList = new ArrayList<>();
        for (NeededItem item : neededMap.values()) {
            if (item.getAmount() > 0.001) {
                double roundedAmount = Math.round(item.getAmount() * 100.0) / 100.0;
                shoppingList.add(ShoppingItemResponse.builder()
                        .name(item.getDisplayName())
                        .quantity(roundedAmount)
                        .unit(item.getUnit())
                        .status(ShoppingItemStatus.PENDING)
                        .build());
            }
        }

        return shoppingList;
    }

    private MealPlan findOwnedMealPlan(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        return mealPlanRepository.findByIdAndUserId(id, userId)
                .orElseThrow(MealPlanNotFoundException::new);
    }
}
