package org.group12.feature.cookbook.repository;

import org.springframework.data.repository.query.Param;
import org.group12.feature.cookbook.entity.SavedRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedRecipeRepository extends JpaRepository<SavedRecipe, UUID> {
    Optional<SavedRecipe> findByUserIdAndRecipeId(UUID userId, UUID recipeId);

    @Query("""
        select s from SavedRecipe s
        join fetch s.recipe
        where s.user.id = :userId
          and (:cursor is null or s.id > :cursor)
          and (:favorite is null or s.favorite = :favorite)
          and (:collectionId is null or exists (
                select 1 from Collection c join c.savedRecipes sr
                where c.id = :collectionId
                  and c.user.id = :userId
                  and sr.id = s.id))
        order by s.id asc
        """)
    List<SavedRecipe> findPage(@Param("userId") UUID userId,
                               @Param("cursor") UUID cursor,
                               @Param("favorite") Boolean favorite,
                               @Param("collectionId") UUID collectionId,
                               Pageable pageable);

    @Query("""
    select s from SavedRecipe s
    join fetch s.recipe r
    left join fetch r.cuisine
    where s.id = :id and s.user.id = :userId
    """)
    Optional<SavedRecipe> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    long countByUserId(UUID userId);

    boolean existsByUserIdAndRecipeId(UUID userId, UUID recipeId);
}
