package org.group12.feature.ingredient.repository;

import org.group12.feature.ingredient.entity.ExcludedIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExcludedIngredientRepository extends JpaRepository<ExcludedIngredient, UUID> {

    List<ExcludedIngredient> findAllByUserIdOrderByCreatedAtAsc(UUID userId);

    Optional<ExcludedIngredient> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndNameIgnoreCase(UUID userId, String name);

    List<ExcludedIngredient> findByUserId(UUID userId);
}
