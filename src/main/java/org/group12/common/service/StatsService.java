package org.group12.common.service;

import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.UserStatsResponse;
import org.group12.common.utils.AppUtil;
import org.group12.feature.cookbook.repository.CollectionRepository;
import org.group12.feature.cookbook.repository.SavedRecipeRepository;
import org.group12.feature.mealplan.repository.MealPlanEntryRepository;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final SavedRecipeRepository savedRecipeRepository;
    private final CollectionRepository collectionRepository;
    private final MealPlanEntryRepository mealPlanEntryRepository;
    private final PantryItemRepository pantryItemRepository;
    private final AppUtil appUtil;

    @Transactional(readOnly = true)
    public UserStatsResponse getMyStats() {
        UUID userId = appUtil.userIdFromAuthentication();
        return new UserStatsResponse(
                savedRecipeRepository.countByUserId(userId),
                collectionRepository.countByUserId(userId),
                mealPlanEntryRepository.countCookedByUserId(userId),
                pantryItemRepository.countExpiredItemsByUserId(userId)
        );
    }
}