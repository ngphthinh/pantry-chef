package org.group12.feature.pantry.repository;

import org.group12.feature.pantry.entity.PantryItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.group12.feature.pantry.enums.StorageLocation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PantryItemRepository extends JpaRepository<PantryItem, UUID> {
    @Query("""
            select p from PantryItem p join fetch p.ingredient where p.user.id = :userId
            """)
    List<PantryItem> findByUserIdNotIngredientCategory(UUID userId);

    @EntityGraph(attributePaths = {"ingredient", "ingredient.category"})
    Optional<PantryItem> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = {"ingredient", "ingredient.category"})
    List<PantryItem> findByUserId(UUID userId);

    @Query("""
                SELECT p FROM PantryItem p
                JOIN FETCH p.ingredient i
                LEFT JOIN FETCH i.category c
                WHERE p.user.id = :userId
                  AND (:categoryId IS NULL OR c.id = :categoryId)
                  AND (:location IS NULL OR p.location = :location)
                  AND (:keyword IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                ORDER BY p.id DESC
            """)
    List<PantryItem> findItemsFirstPage(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId,
            @Param("location") StorageLocation location,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
                SELECT p FROM PantryItem p
                JOIN FETCH p.ingredient i
                LEFT JOIN FETCH i.category c
                WHERE p.user.id = :userId
                  AND p.id < :cursor
                  AND (:categoryId IS NULL OR c.id = :categoryId)
                  AND (:location IS NULL OR p.location = :location)
                  AND (:keyword IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                ORDER BY p.id DESC
            """)
    List<PantryItem> findItemsNextPage(
            @Param("userId") UUID userId,
            @Param("cursor") UUID cursor,
            @Param("categoryId") UUID categoryId,
            @Param("location") StorageLocation location,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
                SELECT p FROM PantryItem p
                JOIN FETCH p.ingredient i
                LEFT JOIN FETCH i.category c
                WHERE p.user.id = :userId
                  AND p.expiryDate IS NOT NULL
                  AND p.expiryDate <= :thresholdDate
                ORDER BY p.expiryDate ASC
            """)
    List<PantryItem> findExpiringItems(
            @Param("userId") UUID userId,
            @Param("thresholdDate") LocalDate thresholdDate
    );

    @Query("""
            SELECT COUNT(p)
            FROM PantryItem p
            WHERE p.user.id = :userId
              AND p.expiryDate IS NOT NULL
              AND p.expiryDate < CURRENT_DATE
            """)
    long countExpiredItemsByUserId(@Param("userId") UUID userId);

    @Query("""
        select distinct i.ingredient.name
        from PantryItem i
        where i.user.id = :userId
        """)
    List<String> findDistinctIngredientNames(@Param("userId") UUID userId, Pageable pageable);
}
