package org.group12.feature.ingredient.repository;

import org.group12.feature.ingredient.entity.IngredientCategory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IngredientCategoryRepository extends JpaRepository<IngredientCategory, UUID> {

    /** Danh mục người dùng được thấy: của hệ thống (owner NULL) hoặc do chính họ tạo. */
    @Query("SELECT c FROM IngredientCategory c WHERE c.owner IS NULL OR c.owner.id = :userId")
    List<IngredientCategory> findVisible(@Param("userId") UUID userId, Sort sort);

    @Query("""
            SELECT c FROM IngredientCategory c
            WHERE c.id = :id AND (c.owner IS NULL OR c.owner.id = :userId)
            """)
    Optional<IngredientCategory> findVisibleById(@Param("id") UUID id, @Param("userId") UUID userId);
    boolean existsByOwnerIsNull();

    boolean existsByCodeAndOwnerIsNull(String code);

    boolean existsByCodeAndOwnerId(String code, UUID ownerId);

    Optional<IngredientCategory> findByCodeAndOwnerIsNull(String code);

    List<IngredientCategory> findAllByOwnerIsNull();

    List<IngredientCategory> findAllByCodeInAndOwnerIsNull(Collection<String> codes);
}