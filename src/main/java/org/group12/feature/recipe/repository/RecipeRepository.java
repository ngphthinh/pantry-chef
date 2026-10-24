package org.group12.feature.recipe.repository;

import org.group12.feature.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
    Optional<Recipe> findByIdAndCustomTrue(UUID id);

    @Query("select r from Recipe r where lower(r.title) in :titles")
    List<Recipe> findByLowerTitleIn(@Param("titles") Collection<String> titles);
}
