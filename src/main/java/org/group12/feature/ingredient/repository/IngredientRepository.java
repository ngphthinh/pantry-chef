package org.group12.feature.ingredient.repository;

import org.group12.feature.ingredient.entity.Ingredient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID>, JpaSpecificationExecutor<Ingredient> {

    Optional<Ingredient> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, UUID id);

    @Query("""
            SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
            FROM Ingredient i
            WHERE i.category.id = :categoryId
            """)
    boolean existsAnyByCategoryId(@Param("categoryId") UUID categoryId);
    @Query("""
        SELECT i FROM Ingredient i
        LEFT JOIN FETCH i.category c
        WHERE i.id > :cursor
          AND (:allCategories = true OR c.id = :categoryId)
          AND lower(cast(function('translate', i.name,
                  :#{T(org.group12.common.utils.TextUtil).ACCENT_FROM},
                  :#{T(org.group12.common.utils.TextUtil).ACCENT_TO}) as String))
              LIKE :pattern ESCAPE '!'
        ORDER BY i.id ASC
    """)
    List<Ingredient> searchIngredients(
            @Param("cursor") UUID cursor,
            @Param("allCategories") boolean allCategories,
            @Param("categoryId") UUID categoryId,
            @Param("pattern") String pattern,
            Pageable pageable
    );
}
