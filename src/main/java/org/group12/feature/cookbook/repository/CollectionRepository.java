package org.group12.feature.cookbook.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.group12.feature.cookbook.dto.response.CollectionResponse;
import org.group12.feature.cookbook.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {
    @Query("""
            select new org.group12.feature.cookbook.dto.response.CollectionResponse(
                c.id, c.name, count(sr))
            from Collection c
            left join c.savedRecipes sr
            where c.user.id = :userId
            group by c.id, c.name
            order by c.name asc
            """)
    List<CollectionResponse> findAllByUser(@Param("userId") UUID userId);


    @Query("""
            select c from Collection c
            left join fetch c.savedRecipes sr
            where c.id = :id and c.user.id = :userId
            """)
    Optional<Collection> findCollectionByIdAndUserId (UUID id, UUID userId);

    Optional<Collection> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndNameIgnoreCaseAndIdNot(UUID userId, String name, UUID id);

    boolean existsByUserIdAndNameIgnoreCase(UUID userId, String name);

    @Query("""
            select count(sr) from Collection c
            left join c.savedRecipes sr
            where c.id = :id
            """)
    long countRecipes(@Param("id") UUID id);

    @Query("""
            select c from Collection c
            left join fetch c.savedRecipes
            where c.id = :id and c.user.id = :userId
            """)
    Optional<Collection> findWithRecipesByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    long countByUserId(UUID userId);

}
