package org.group12.feature.shopping.service;

import org.group12.common.enums.Unit;
import org.group12.common.utils.AppUtil;
import org.group12.feature.shopping.dto.request.ShoppingItemCreateRequest;
import org.group12.feature.shopping.dto.request.ShoppingItemUpdateRequest;
import org.group12.feature.shopping.dto.response.ShoppingItemResponse;
import org.group12.feature.shopping.entity.ShoppingItem;
import org.group12.feature.shopping.entity.ShoppingList;
import org.group12.feature.shopping.enums.ShoppingItemStatus;
import org.group12.feature.shopping.exception.ShoppingItemNotFoundException;
import org.group12.feature.shopping.exception.ShoppingListNotFoundException;
import org.group12.feature.shopping.mapper.ShoppingItemMapper;
import org.group12.feature.shopping.repository.ShoppingItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingItemServiceTest {

    private static final UUID LIST_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();

    @Mock
    private ShoppingItemRepository shoppingItemRepository;
    @Mock
    private ShoppingListService shoppingListService;
    @Mock
    private ShoppingItemMapper shoppingItemMapper;
    @Spy
    private AppUtil appUtil = new AppUtil();

    @InjectMocks
    private ShoppingItemService service;

    @Test
    void create_setsPendingStatusIdListAndTrimsName() {
        ShoppingList list = ShoppingList.builder().id(LIST_ID).name("Weekend").build();
        ShoppingItemCreateRequest request = new ShoppingItemCreateRequest("  Milk ", 2.0, Unit.L);
        ShoppingItem mapped = ShoppingItem.builder().name("  Milk ").quantity(2.0).unit(Unit.L).build();
        ShoppingItemResponse response = ShoppingItemResponse.builder().name("Milk").build();

        when(shoppingListService.getOwnedList(LIST_ID)).thenReturn(list);
        when(shoppingItemMapper.toShoppingItem(request)).thenReturn(mapped);
        when(shoppingItemRepository.save(any(ShoppingItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(shoppingItemMapper.toShoppingItemResponse(any(ShoppingItem.class))).thenReturn(response);

        ShoppingItemResponse result = service.create(LIST_ID, request);

        assertThat(result).isSameAs(response);
        ArgumentCaptor<ShoppingItem> captor = ArgumentCaptor.forClass(ShoppingItem.class);
        verify(shoppingItemRepository).save(captor.capture());
        ShoppingItem saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Milk");
        assertThat(saved.getStatus()).isEqualTo(ShoppingItemStatus.PENDING);
        assertThat(saved.getShoppingList()).isSameAs(list);
    }

    @Test
    void create_inOtherUsersList_throwsAndSavesNothing() {
        when(shoppingListService.getOwnedList(LIST_ID)).thenThrow(new ShoppingListNotFoundException());

        assertThatThrownBy(() -> service.create(LIST_ID, new ShoppingItemCreateRequest("Milk", null, null)))
                .isInstanceOf(ShoppingListNotFoundException.class);
        verifyNoInteractions(shoppingItemRepository);
    }

    @Test
    void update_appliesPatchAndReturnsResponse() {
        ShoppingList list = ShoppingList.builder().id(LIST_ID).build();
        ShoppingItem item = ShoppingItem.builder()
                .id(ITEM_ID).name("Milk").status(ShoppingItemStatus.PENDING).build();
        ShoppingItemUpdateRequest request = ShoppingItemUpdateRequest.builder()
                .status(ShoppingItemStatus.BOUGHT).build();
        ShoppingItemResponse response = ShoppingItemResponse.builder().status(ShoppingItemStatus.BOUGHT).build();

        when(shoppingListService.getOwnedList(LIST_ID)).thenReturn(list);
        when(shoppingItemRepository.findByIdAndShoppingListId(ITEM_ID, LIST_ID)).thenReturn(Optional.of(item));
        doAnswer(inv -> {
            ((ShoppingItem) inv.getArgument(1)).setStatus(((ShoppingItemUpdateRequest) inv.getArgument(0)).getStatus());
            return null;
        }).when(shoppingItemMapper).updateShoppingItemEntity(eq(request), eq(item));
        when(shoppingItemMapper.toShoppingItemResponse(item)).thenReturn(response);

        ShoppingItemResponse result = service.update(LIST_ID, ITEM_ID, request);

        assertThat(result).isSameAs(response);
        assertThat(item.getStatus()).isEqualTo(ShoppingItemStatus.BOUGHT);
        assertThat(item.getName()).isEqualTo("Milk"); // trường không gửi thì giữ nguyên
    }

    @Test
    void update_missingItem_throwsNotFound() {
        when(shoppingListService.getOwnedList(LIST_ID)).thenReturn(ShoppingList.builder().id(LIST_ID).build());
        when(shoppingItemRepository.findByIdAndShoppingListId(ITEM_ID, LIST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(LIST_ID, ITEM_ID, new ShoppingItemUpdateRequest()))
                .isInstanceOf(ShoppingItemNotFoundException.class);
        verify(shoppingItemMapper, never()).updateShoppingItemEntity(any(ShoppingItemUpdateRequest.class), any(ShoppingItem.class));
    }

    @Test
    void delete_removesItemOfOwnedList() {
        ShoppingItem item = ShoppingItem.builder().id(ITEM_ID).name("Milk").build();
        when(shoppingListService.getOwnedList(LIST_ID)).thenReturn(ShoppingList.builder().id(LIST_ID).build());
        when(shoppingItemRepository.findByIdAndShoppingListId(ITEM_ID, LIST_ID)).thenReturn(Optional.of(item));

        service.delete(LIST_ID, ITEM_ID);

        verify(shoppingItemRepository).delete(item);
    }

    @Test
    void delete_inOtherUsersList_throwsBeforeLookingUpItem() {
        when(shoppingListService.getOwnedList(LIST_ID)).thenThrow(new ShoppingListNotFoundException());

        assertThatThrownBy(() -> service.delete(LIST_ID, ITEM_ID)).isInstanceOf(ShoppingListNotFoundException.class);
        verifyNoInteractions(shoppingItemRepository);
    }
}
