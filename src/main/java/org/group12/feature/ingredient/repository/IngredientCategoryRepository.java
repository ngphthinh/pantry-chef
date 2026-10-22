package org.group12.feature.ingredient.repository;

import org.group12.feature.ingredient.entity.IngredientCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngredientCategoryRepository extends JpaRepository<IngredientCategory, UUID> {
    Optional<IngredientCategory> findByCode(String code);
}
