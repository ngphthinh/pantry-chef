package org.group12.feature.mealplan.repository;

import org.group12.feature.mealplan.entity.MealPlanEntry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealPlanEntryRepository extends JpaRepository<MealPlanEntry, UUID> {

    @EntityGraph(attributePaths = {"recipe"})
    Optional<MealPlanEntry> findByIdAndMealPlanId(UUID id, UUID mealPlanId);

    @EntityGraph(attributePaths = {"recipe", "mealPlan"})
    Optional<MealPlanEntry> findByIdAndMealPlanUserId(UUID id, UUID userId);
}
