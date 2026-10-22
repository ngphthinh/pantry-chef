package org.group12.feature.ingredient.repository;

import org.group12.feature.ingredient.entity.IngredientCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngredientCategoryRepository extends JpaRepository<IngredientCategory, UUID> {
    Optional<IngredientCategory> findByCode(String code);

    boolean existsByCode(String code);

    List<IngredientCategory> findAllByCodeIn(Collection<String> codes);
}
