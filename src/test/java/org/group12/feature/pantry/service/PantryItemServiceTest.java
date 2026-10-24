package org.group12.feature.pantry.service;

import org.group12.common.enums.Unit;
import org.group12.common.service.MediaStorageService;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.entity.User;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.ingredient.entity.Ingredient;
import org.group12.feature.ingredient.repository.IngredientRepository;
import org.group12.feature.pantry.dto.request.PantryItemConsumeRequest;
import org.group12.feature.pantry.dto.request.PantryItemCreateRequest;
import org.group12.feature.pantry.dto.response.PantryItemResponse;
import org.group12.feature.pantry.entity.PantryItem;
import org.group12.feature.pantry.enums.StorageLocation;
import org.group12.feature.pantry.exception.InsufficientQuantityException;
import org.group12.feature.pantry.mapper.PantryItemMapper;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PantryItemServiceTest {

    @Mock
    private PantryItemRepository pantryItemRepository;
    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PantryItemMapper pantryItemMapper;
    @Mock
    private MediaStorageService mediaStorageService;
    @Mock
    private AppUtil appUtil;

    @InjectMocks
    private PantryItemService pantryItemService;

    private UUID userId;
    private UUID itemId;
    private UUID ingredientId;
    private Ingredient ingredient;
    private User user;
    private PantryItem pantryItem;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        ingredientId = UUID.randomUUID();

        user = User.builder().id(userId).email("user@test.com").build();
        ingredient = Ingredient.builder().id(ingredientId).name("Trứng gà").defaultUnit(Unit.PCS).build();
        pantryItem = PantryItem.builder()
                .id(itemId)
                .user(user)
                .ingredient(ingredient)
                .quantity(10.0)
                .unit(Unit.PCS)
                .location(StorageLocation.FRIDGE)
                .expiryDate(LocalDate.now().plusDays(5))
                .build();
    }

    @Test
    void create_Success() {
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
        when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.of(ingredient));
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(appUtil.generateUUID()).thenReturn(itemId);

        PantryItemCreateRequest request = PantryItemCreateRequest.builder()
                .ingredientId(ingredientId)
                .quantity(10.0)
                .unit(Unit.PCS)
                .location(StorageLocation.FRIDGE)
                .build();

        when(pantryItemMapper.toEntity(request)).thenReturn(pantryItem);
        when(pantryItemRepository.save(any(PantryItem.class))).thenReturn(pantryItem);
        when(pantryItemMapper.toResponse(pantryItem)).thenReturn(PantryItemResponse.builder().id(itemId).quantity(10.0).build());

        PantryItemResponse response = pantryItemService.create(request);

        assertNotNull(response);
        assertEquals(itemId, response.getId());
        verify(pantryItemRepository, times(1)).save(any(PantryItem.class));
    }

    @Test
    void consume_Success() {
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
        when(pantryItemRepository.findByIdAndUserId(itemId, userId)).thenReturn(Optional.of(pantryItem));

        PantryItemConsumeRequest request = PantryItemConsumeRequest.builder().amount(3.0).build();
        when(pantryItemMapper.toResponse(pantryItem)).thenReturn(PantryItemResponse.builder().id(itemId).quantity(7.0).build());

        PantryItemResponse response = pantryItemService.consume(itemId, request);

        assertNotNull(response);
        assertEquals(7.0, pantryItem.getQuantity());
    }

    @Test
    void consume_InsufficientQuantity_ThrowsException() {
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
        when(pantryItemRepository.findByIdAndUserId(itemId, userId)).thenReturn(Optional.of(pantryItem));

        PantryItemConsumeRequest request = PantryItemConsumeRequest.builder().amount(15.0).build();

        assertThrows(InsufficientQuantityException.class, () -> pantryItemService.consume(itemId, request));
    }

    @Test
    void getExpiringItems_Success() {
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
        when(pantryItemRepository.findExpiringItems(eq(userId), any(LocalDate.class))).thenReturn(List.of(pantryItem));
        when(pantryItemMapper.toResponse(pantryItem)).thenReturn(PantryItemResponse.builder().id(itemId).build());

        List<PantryItemResponse> list = pantryItemService.getExpiringItems(3);

        assertEquals(1, list.size());
        verify(pantryItemRepository, times(1)).findExpiringItems(eq(userId), any(LocalDate.class));
    }

    @Test
    void delete_Success() {
        when(appUtil.userIdFromAuthentication()).thenReturn(userId);
        when(pantryItemRepository.findByIdAndUserId(itemId, userId)).thenReturn(Optional.of(pantryItem));

        pantryItemService.delete(itemId);

        verify(pantryItemRepository, times(1)).delete(pantryItem);
    }
}
