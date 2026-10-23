package org.group12.feature.pantry.repository;

import org.group12.feature.pantry.entity.PantryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PantryItemRepository extends JpaRepository<PantryItem, UUID> {
    @Query(value = """
        select count(*) from pantry_items
        where user_id = :userId
          and expiry_date is not null
          and expiry_date < current_date
        """, nativeQuery = true)
    long countExpiredByUserId(@Param("userId") UUID userId);
}
