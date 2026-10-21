package org.group12.feature.recipe.repository;

import org.group12.feature.recipe.entity.Cuisine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CuisineRepository extends JpaRepository<Cuisine, UUID> {

    List<Cuisine> findAllByOrderByNameAsc();

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM recipes WHERE cuisine_id = :cuisineId)", nativeQuery = true)
    boolean isUsedByRecipe(@Param("cuisineId") UUID cuisineId);
}
