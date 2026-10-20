package org.group12.feature.shopping.service;

import org.group12.common.dto.response.CursorResponse;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.entity.User;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.shopping.dto.request.ShoppingListCreateRequest;
import org.group12.feature.shopping.dto.request.ShoppingListUpdateRequest;
import org.group12.feature.shopping.dto.response.ShoppingItemResponse;
import org.group12.feature.shopping.dto.response.ShoppingListDetailResponse;
import org.group12.feature.shopping.dto.response.ShoppingListResponse;
import org.group12.feature.shopping.entity.ShoppingItem;
import org.group12.feature.shopping.entity.ShoppingList;
import org.group12.feature.shopping.exception.ShoppingListNotFoundException;
import org.group12.feature.shopping.mapper.ShoppingItemMapper;
import org.group12.feature.shopping.mapper.ShoppingListMapper;
import org.group12.feature.shopping.repository.ShoppingItemRepository;
import org.group12.feature.shopping.repository.ShoppingListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID LIST_ID = UUID.randomUUID();

    @Mock
    private ShoppingListRepository shoppingListRepository;
    @Mock
    private ShoppingItemRepository shoppingItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ShoppingListMapper shoppingListMapper;
    @Mock
    private ShoppingItemMapper shoppingItemMapper;
    @Spy
    private AppUtil appUtil = new AppUtil();

    @InjectMocks
    private ShoppingListService service;

    private void authenticateAsUser() {
        doReturn(USER_ID).when(appUtil).userIdFromAuthentication();
    }

    @Test
    void create_assignsIdAndOwnerAndTrimsName() {
        authenticateAsUser();
        ShoppingListCreateRequest request = new ShoppingListCreateRequest("  Weekend  ");
        ShoppingList mapped = ShoppingList.builder().name("  Weekend  ").build();
        User userRef = User.builder().id(USER_ID).build();
        ShoppingListResponse response = ShoppingListResponse.builder().name("Weekend").build();

        when(shoppingListMapper.toEntity(request)).thenReturn(mapped);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(userRef);
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(inv -> inv.getArgument(0));
        when(shoppingListMapper.toResponse(any(ShoppingList.class))).thenReturn(response);

        ShoppingListResponse result = service.create(request);

        assertThat(result).isSameAs(response);
        ArgumentCaptor<ShoppingList> captor = ArgumentCaptor.forClass(ShoppingList.class);
        verify(shoppingListRepository).save(captor.capture());
        ShoppingList saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Weekend");
        assertThat(saved.getUser()).isSameAs(userRef);
    }

    @Test
    void getMyLists_firstPage_hasNextWhenMoreThanSize() {
        authenticateAsUser();
        ShoppingList l1 = ShoppingList.builder().id(UUID.randomUUID()).name("A").build();
        ShoppingList l2 = ShoppingList.builder().id(UUID.randomUUID()).name("B").build();
        ShoppingList l3 = ShoppingList.builder().id(UUID.randomUUID()).name("C").build();

        when(shoppingListRepository.findByUserIdOrderByIdDesc(eq(USER_ID), any(Pageable.class)))
                .thenReturn(List.of(l1, l2, l3));
        when(shoppingListMapper.toResponse(any(ShoppingList.class)))
                .thenAnswer(inv -> ShoppingListResponse.builder()
                        .id(((ShoppingList) inv.getArgument(0)).getId())
                        .build());

        CursorResponse<ShoppingListResponse> result = service.getMyLists(null, 2);

        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getNextCursor()).isEqualTo(l2.getId().toString());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(shoppingListRepository).findByUserIdOrderByIdDesc(eq(USER_ID), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(3); // size + 1 để biết còn trang sau
    }

    @Test
    void getMyLists_withCursor_usesNextPageQuery_andCapsSizeAt50() {
        authenticateAsUser();
        UUID cursor = UUID.randomUUID();
        when(shoppingListRepository.findByUserIdAndIdLessThanOrderByIdDesc(eq(USER_ID), eq(cursor), any(Pageable.class)))
                .thenReturn(List.of());

        CursorResponse<ShoppingListResponse> result = service.getMyLists(cursor, 1000);

        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getNextCursor()).isNull();

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(shoppingListRepository)
                .findByUserIdAndIdLessThanOrderByIdDesc(eq(USER_ID), eq(cursor), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(51);
    }

    @Test
    void getDetail_returnsItemsOfOwnedList() {
        authenticateAsUser();
        ShoppingList list = ShoppingList.builder().id(LIST_ID).name("Weekend").build();
        ShoppingItem item = ShoppingItem.builder().id(UUID.randomUUID()).name("Egg").build();
        ShoppingItemResponse itemResponse = ShoppingItemResponse.builder().name("Egg").build();

        when(shoppingListRepository.findByIdAndUserId(LIST_ID, USER_ID)).thenReturn(Optional.of(list));
        when(shoppingItemRepository.findByShoppingListIdOrderByIdAsc(LIST_ID)).thenReturn(List.of(item));
        when(shoppingListMapper.toDetailResponse(list))
                .thenReturn(ShoppingListDetailResponse.builder().id(LIST_ID).name("Weekend").build());
        when(shoppingItemMapper.toResponse(item)).thenReturn(itemResponse);

        ShoppingListDetailResponse result = service.getDetail(LIST_ID);

        assertThat(result.getId()).isEqualTo(LIST_ID);
        assertThat(result.getItems()).containsExactly(itemResponse);
    }

    @Test
    void getDetail_ofOtherUsersList_throwsNotFound() {
        authenticateAsUser();
        when(shoppingListRepository.findByIdAndUserId(LIST_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDetail(LIST_ID)).isInstanceOf(ShoppingListNotFoundException.class);
        verifyNoInteractions(shoppingItemRepository);
    }

    @Test
    void update_appliesRequestAndTrimsName() {
        authenticateAsUser();
        ShoppingList list = ShoppingList.builder().id(LIST_ID).name("Old").build();
        ShoppingListUpdateRequest request = new ShoppingListUpdateRequest("  Renamed  ");
        ShoppingListResponse response = ShoppingListResponse.builder().name("Renamed").build();

        when(shoppingListRepository.findByIdAndUserId(LIST_ID, USER_ID)).thenReturn(Optional.of(list));
        doAnswer(inv -> {
            ((ShoppingList) inv.getArgument(1)).setName(((ShoppingListUpdateRequest) inv.getArgument(0)).getName());
            return null;
        }).when(shoppingListMapper).updateEntity(eq(request), eq(list));
        when(shoppingListMapper.toResponse(list)).thenReturn(response);

        ShoppingListResponse result = service.update(LIST_ID, request);

        assertThat(result).isSameAs(response);
        assertThat(list.getName()).isEqualTo("Renamed");
    }

    @Test
    void delete_removesOwnedList() {
        authenticateAsUser();
        ShoppingList list = ShoppingList.builder().id(LIST_ID).name("Weekend").build();
        when(shoppingListRepository.findByIdAndUserId(LIST_ID, USER_ID)).thenReturn(Optional.of(list));

        service.delete(LIST_ID);

        verify(shoppingListRepository).delete(list);
    }

    @Test
    void delete_ofOtherUsersList_throwsNotFoundAndDeletesNothing() {
        authenticateAsUser();
        when(shoppingListRepository.findByIdAndUserId(LIST_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(LIST_ID)).isInstanceOf(ShoppingListNotFoundException.class);
        verify(shoppingListRepository, org.mockito.Mockito.never()).delete(any(ShoppingList.class));
    }
}
