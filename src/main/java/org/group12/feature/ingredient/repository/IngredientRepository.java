package org.group12.feature.ingredient.repository;

import org.group12.feature.ingredient.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID>, JpaSpecificationExecutor<Ingredient> {

    Optional<Ingredient> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, UUID id);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM ingredients WHERE category_id = :categoryId)", nativeQuery = true)
    boolean existsAnyByCategoryId(@Param("categoryId") UUID categoryId);
}
