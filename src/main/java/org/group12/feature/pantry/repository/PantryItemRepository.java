package org.group12.feature.pantry.repository;

import org.group12.feature.pantry.entity.PantryItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PantryItemRepository extends JpaRepository<PantryItem, UUID> {
    @EntityGraph(attributePaths = "ingredient")
    List<PantryItem> findByUserId(UUID userId);
}
