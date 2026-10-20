package org.group12.feature.shopping.repository;

import org.group12.feature.shopping.entity.ShoppingList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, UUID> {

    Optional<ShoppingList> findByIdAndUserId(UUID id, UUID userId);

    List<ShoppingList> findByUserIdOrderByIdDesc(UUID userId, Pageable pageable);

    List<ShoppingList> findByUserIdAndIdLessThanOrderByIdDesc(UUID userId, UUID cursor, Pageable pageable);
}
