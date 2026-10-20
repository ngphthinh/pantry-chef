package org.group12.feature.shopping.repository;

import org.group12.feature.shopping.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, UUID> {

    // id là UUIDv7 nên sắp xếp theo id cũng là sắp xếp theo thời điểm thêm
    List<ShoppingItem> findByShoppingListIdOrderByIdAsc(UUID shoppingListId);

    Optional<ShoppingItem> findByIdAndShoppingListId(UUID id, UUID shoppingListId);
}
