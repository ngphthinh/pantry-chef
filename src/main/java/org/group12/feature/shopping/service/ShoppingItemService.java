package org.group12.feature.shopping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.utils.AppUtil;
import org.group12.feature.shopping.dto.request.ShoppingItemCreateRequest;
import org.group12.feature.shopping.dto.request.ShoppingItemUpdateRequest;
import org.group12.feature.shopping.dto.response.ShoppingItemResponse;
import org.group12.feature.shopping.entity.ShoppingItem;
import org.group12.feature.shopping.entity.ShoppingList;
import org.group12.feature.shopping.enums.ShoppingItemStatus;
import org.group12.feature.shopping.exception.ShoppingItemNotFoundException;
import org.group12.feature.shopping.mapper.ShoppingItemMapper;
import org.group12.feature.shopping.repository.ShoppingItemRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final ShoppingListService shoppingListService;
    private final ShoppingItemMapper shoppingItemMapper;
    private final AppUtil appUtil;

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ShoppingItemResponse create(UUID listId, ShoppingItemCreateRequest request) {
        ShoppingList list = shoppingListService.getOwnedList(listId);

        ShoppingItem item = shoppingItemMapper.toEntity(request);
        item.setId(appUtil.generateUUID());
        item.setName(item.getName().trim());
        item.setStatus(ShoppingItemStatus.PENDING);
        item.setShoppingList(list);

        item = shoppingItemRepository.save(item);
        log.info("Added shopping item {} to list {}", item.getId(), listId);
        return shoppingItemMapper.toResponse(item);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ShoppingItemResponse update(UUID listId, UUID itemId, ShoppingItemUpdateRequest request) {
        ShoppingItem item = findItemInOwnedList(listId, itemId);
        shoppingItemMapper.updateEntity(request, item);   // dirty checking tự ghi, không cần save()
        item.setName(item.getName().trim());
        return shoppingItemMapper.toResponse(item);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void delete(UUID listId, UUID itemId) {
        shoppingItemRepository.delete(findItemInOwnedList(listId, itemId));   // @SQLDelete -> xóa mềm
    }

    // Kiểm tra danh sách thuộc người dùng hiện tại trước, rồi mới tìm mục trong danh sách đó
    private ShoppingItem findItemInOwnedList(UUID listId, UUID itemId) {
        shoppingListService.getOwnedList(listId);
        return shoppingItemRepository.findByIdAndShoppingListId(itemId, listId)
                .orElseThrow(ShoppingItemNotFoundException::new);
    }
}
