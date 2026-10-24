package org.group12.feature.mealplan.repository;

import org.group12.feature.mealplan.entity.MealPlanEntry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealPlanEntryRepository extends JpaRepository<MealPlanEntry, UUID> {

    @EntityGraph(attributePaths = {"recipe"})
    Optional<MealPlanEntry> findByIdAndMealPlanId(UUID id, UUID mealPlanId);

    @EntityGraph(attributePaths = {"recipe", "mealPlan"})
    Optional<MealPlanEntry> findByIdAndMealPlanUserId(UUID id, UUID userId);
    @Query("""
        select count(e) from MealPlanEntry e
        where e.mealPlan.user.id = :userId
          and e.cooked = true
        """)
    long countCookedByUserId(@Param("userId") UUID userId);
}
