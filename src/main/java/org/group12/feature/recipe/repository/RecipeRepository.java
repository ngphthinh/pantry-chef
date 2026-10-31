package org.group12.feature.recipe.repository;

import org.group12.feature.recipe.entity.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.*;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
    Optional<Recipe> findByIdAndCustomTrue(UUID id);

    @Query("select r from Recipe r where lower(r.title) in :titles")
    List<Recipe> findByLowerTitleIn(@Param("titles") Collection<String> titles);
    Optional<Recipe> findByExternalId(String externalId);

    /** Công thức (từ API ngoài) kèm ẩm thực và nguyên liệu; các bước tải lười khi map (cùng một transaction). */
    @Query("""
            select r from Recipe r
            left join fetch r.cuisine
            left join fetch r.ingredients
            where r.externalId = :externalId
            """)
    Optional<Recipe> findDetailByExternalId(@Param("externalId") String externalId);

    /** Công thức tự tạo của một người dùng, kèm ẩm thực và nguyên liệu. */
    @Query("""
            select r from Recipe r
            left join fetch r.cuisine
            left join fetch r.ingredients
            where r.id = :id and r.custom = true and r.owner.id = :ownerId
            """)
    Optional<Recipe> findCustomDetailByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);

    Optional<Recipe> findByIdAndOwnerIdAndCustomTrue(UUID id, UUID ownerId);

    @EntityGraph(attributePaths = "cuisine")
    List<Recipe> findByCustomTrueAndOwnerIdOrderByIdDesc(UUID ownerId, Pageable pageable);

    @EntityGraph(attributePaths = "cuisine")
    List<Recipe> findByCustomTrueAndOwnerIdAndIdLessThanOrderByIdDesc(UUID ownerId, UUID cursor, Pageable pageable);

    /**
     * Công thức dùng được cho AI lên thực đơn: không phải công thức tự tạo của người khác, đúng ẩm thực / thời gian
     * (tham số null = không lọc), không chứa nguyên liệu loại trừ (tên chữ thường, truyền {@code List.of("")} khi không có).
     */
    @Query("""
            select r from Recipe r
            left join r.cuisine c
            where (r.custom = false or r.owner.id = :userId)
              and (c.id = :cuisineId or :cuisineId is null)
              and (r.readyInMinutes <= :maxMinutes or :maxMinutes is null)
              and exists (select 1 from RecipeIngredient x where x.recipe = r)
              and not exists (
                    select 1 from RecipeIngredient i
                    where i.recipe = r and lower(i.name) in :excluded)
            order by r.id desc
            """)
    List<Recipe> findCandidates(@Param("userId") UUID userId,
                                @Param("cuisineId") UUID cuisineId,
                                @Param("maxMinutes") Integer maxMinutes,
                                @Param("excluded") Collection<String> excluded,
                                Pageable pageable);

    /** Một dòng (công thức, tên nguyên liệu) dùng để tìm và chấm điểm trong bộ nhớ. */
    interface LocalIngredientRow {
        UUID getRecipeId();

        String getName();
    }

    /** Nguyên liệu của mọi công thức người dùng được xem: công thức không phải tự tạo + công thức tự tạo của chính họ. */
    @Query("""
            select r.id as recipeId, i.name as name
            from Recipe r join r.ingredients i
            where r.custom = false or r.owner.id = :userId
            """)
    List<LocalIngredientRow> findAccessibleIngredientRows(@Param("userId") UUID userId);

    @Query("""
            select i.recipe.id as recipeId, i.name as name
            from RecipeIngredient i
            where i.recipe.id in :ids
            """)
    List<LocalIngredientRow> findIngredientRowsByRecipeIds(@Param("ids") Collection<UUID> ids);

    /** {@code pattern} là mẫu LIKE chữ thường, ký tự đặc biệt đã được escape bằng dấu gạch chéo ngược. */
    @EntityGraph(attributePaths = "cuisine")
    @Query("""
            select r from Recipe r
            where (r.custom = false or r.owner.id = :userId)
              and lower(r.title) like :pattern escape '\\'
            order by r.id desc
            """)
    List<Recipe> findAccessibleByTitle(@Param("userId") UUID userId,
                                       @Param("pattern") String pattern,
                                       Pageable pageable);

    @EntityGraph(attributePaths = "cuisine")
    List<Recipe> findByIdIn(Collection<UUID> ids);

    /** Cặp (externalId, id) của các công thức từ API ngoài đã có trong DB. */
    interface ExternalIdRow {
        String getExternalId();

        UUID getId();
    }

    @Query("select r.externalId as externalId, r.id as id from Recipe r where r.externalId in :externalIds")
    List<ExternalIdRow> findIdsByExternalIdIn(@Param("externalIds") Collection<String> externalIds);

    /** Khóa dòng để hai request cùng bổ sung chi tiết cho một công thức không ghi đè nhau. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Recipe r where r.externalId = :externalId")
    Optional<Recipe> findForUpdateByExternalId(@Param("externalId") String externalId);
}
