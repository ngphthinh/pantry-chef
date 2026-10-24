package org.group12.feature.mealplan.repository;

import org.group12.feature.mealplan.entity.MealPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, UUID> {

    @EntityGraph(attributePaths = {"entries", "entries.recipe"})
    Optional<MealPlan> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = {"entries", "entries.recipe"})
    Optional<MealPlan> findByUserIdAndWeekStart(UUID userId, LocalDate weekStart);

    boolean existsByUserIdAndWeekStart(UUID userId, LocalDate weekStart);
}
