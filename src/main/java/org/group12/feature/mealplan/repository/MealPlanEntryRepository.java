package org.group12.feature.mealplan.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.group12.feature.mealplan.entity.MealPlanEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface MealPlanEntryRepository extends JpaRepository<MealPlanEntry, UUID> {
    @Query("""
        select count(e) from MealPlanEntry e
        where e.mealPlan.user.id = :userId
          and e.cooked = true
        """)
    long countCookedByUserId(@Param("userId") UUID userId);
}
