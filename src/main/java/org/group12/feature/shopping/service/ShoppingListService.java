package org.group12.feature.shopping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.dto.response.CursorResponse;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.shopping.dto.request.ShoppingListCreateRequest;
import org.group12.feature.shopping.dto.request.ShoppingListUpdateRequest;
import org.group12.feature.shopping.dto.response.ShoppingItemResponse;
import org.group12.feature.shopping.dto.response.ShoppingListDetailResponse;
import org.group12.feature.shopping.dto.response.ShoppingListResponse;
import org.group12.feature.shopping.entity.ShoppingList;
import org.group12.feature.shopping.exception.ShoppingListNotFoundException;
import org.group12.feature.shopping.mapper.ShoppingItemMapper;
import org.group12.feature.shopping.mapper.ShoppingListMapper;
import org.group12.feature.shopping.repository.ShoppingItemRepository;
import org.group12.feature.shopping.repository.ShoppingListRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final UserRepository userRepository;
    private final ShoppingListMapper shoppingListMapper;
    private final ShoppingItemMapper shoppingItemMapper;
    private final AppUtil appUtil;

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ShoppingListResponse create(ShoppingListCreateRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();

        ShoppingList list = shoppingListMapper.toEntity(request);
        list.setId(appUtil.generateUUID());
        list.setName(list.getName().trim());
        list.setUser(userRepository.getReferenceById(userId));

        list = shoppingListRepository.save(list);
        log.info("User {} created shopping list {}", userId, list.getId());
        return shoppingListMapper.toResponse(list);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public CursorResponse<ShoppingListResponse> getMyLists(UUID cursor, int size) {
        UUID userId = appUtil.userIdFromAuthentication();
        int limit = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(0, limit + 1);

        List<ShoppingList> lists = cursor == null
                ? shoppingListRepository.findByUserIdOrderByIdDesc(userId, pageable)
                : shoppingListRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, cursor, pageable);

        return appUtil.buildCursorResponse(lists, limit, ShoppingList::getId, shoppingListMapper::toResponse);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional(readOnly = true)
    public ShoppingListDetailResponse getDetail(UUID id) {
        return buildDetail(getOwnedList(id));
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ShoppingListResponse update(UUID id, ShoppingListUpdateRequest request) {
        ShoppingList list = getOwnedList(id);
        shoppingListMapper.updateEntity(request, list);   // dirty checking tự ghi, không cần save()
        list.setName(list.getName().trim());
        return shoppingListMapper.toResponse(list);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void delete(UUID id) {
        shoppingListRepository.delete(getOwnedList(id));   // @SQLDelete -> xóa mềm, cascade xuống các mục
    }

    /**
     * Lấy danh sách thuộc người dùng hiện tại. Danh sách của người khác hoặc không tồn tại đều
     * trả {@link ShoppingListNotFoundException} để không lộ sự tồn tại của bản ghi.
     * Dùng chung với {@code ShoppingItemService} (cùng feature).
     */
    @Transactional(readOnly = true)
    public ShoppingList getOwnedList(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        return shoppingListRepository.findByIdAndUserId(id, userId)
                .orElseThrow(ShoppingListNotFoundException::new);
    }

    private ShoppingListDetailResponse buildDetail(ShoppingList list) {
        List<ShoppingItemResponse> items = shoppingItemRepository.findByShoppingListIdOrderByIdAsc(list.getId())
                .stream()
                .map(shoppingItemMapper::toResponse)
                .toList();

        ShoppingListDetailResponse detail = shoppingListMapper.toDetailResponse(list);
        detail.setItems(items);
        return detail;
    }
}
