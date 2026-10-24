package org.group12.feature.recipe.repository;

import org.group12.feature.recipe.entity.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
