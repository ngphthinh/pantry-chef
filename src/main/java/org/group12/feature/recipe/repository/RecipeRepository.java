package org.group12.feature.recipe.repository;

import org.group12.feature.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.*;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
    Optional<Recipe> findByIdAndCustomTrue(UUID id);

    @Query("select r from Recipe r where lower(r.title) in :titles")
    List<Recipe> findByLowerTitleIn(@Param("titles") Collection<String> titles);

    Optional<Recipe> findByExternalId(UUID externalId);

    @Query("""
            select r from Recipe r
            where (r.custom = false or r.owner.id = :userId)
              and (:cuisineId is null or r.cuisine.id = :cuisineId)
              and (:maxMinutes is null or r.readyInMinutes <= :maxMinutes)
              and not exists (
                  select 1 from RecipeIngredient ri
                  where ri.recipe = r and lower(ri.name) in :excluded)
            """)
    List<Recipe> findCandidates(@Param("userId") UUID userId,
                                @Param("cuisineId") UUID cuisineId,
                                @Param("maxMinutes") Integer maxMinutes,
                                @Param("excluded") Collection<String> excluded,
                                Pageable pageable);

}
