package org.group12.feature.pantry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.dto.response.CursorResponse;
import org.group12.common.dto.response.UploadMediaResponse;
import org.group12.common.enums.MediaType;
import org.group12.common.service.MediaStorageService;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.ingredient.entity.Ingredient;
import org.group12.feature.ingredient.exception.IngredientNotFoundException;
import org.group12.feature.ingredient.repository.IngredientRepository;
import org.group12.feature.pantry.dto.request.PantryItemBatchCreateRequest;
import org.group12.feature.pantry.dto.request.PantryItemConsumeRequest;
import org.group12.feature.pantry.dto.request.PantryItemCreateRequest;
import org.group12.feature.pantry.dto.request.PantryItemUpdateRequest;
import org.group12.feature.pantry.dto.response.PantryItemResponse;
import org.group12.feature.pantry.entity.PantryItem;
import org.group12.feature.pantry.enums.StorageLocation;
import org.group12.feature.pantry.exception.ErrorCodePantry;
import org.group12.feature.pantry.exception.InsufficientQuantityException;
import org.group12.feature.pantry.exception.PantryItemNotFoundException;
import org.group12.feature.pantry.mapper.PantryItemMapper;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableMethodSecurity
@PreAuthorize("hasRole('USER')")
public class PantryItemService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PantryItemRepository pantryItemRepository;
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;
    private final PantryItemMapper pantryItemMapper;
    private final MediaStorageService mediaStorageService;
    private final AppUtil appUtil;

    @Transactional
    public PantryItemResponse create(PantryItemCreateRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(IngredientNotFoundException::new);

        PantryItem item = pantryItemMapper.toEntity(request);
        item.setId(appUtil.generateUUID());
        item.setUser(userRepository.getReferenceById(userId));
        item.setIngredient(ingredient);

        item = pantryItemRepository.saveAndFlush(item);
        log.info("User {} added pantry item {}", userId, item.getId());
        return pantryItemMapper.toResponse(item);
    }

    @Transactional
    public List<PantryItemResponse> createBatch(PantryItemBatchCreateRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();
        List<PantryItem> itemsToSave = new ArrayList<>();

        for (PantryItemCreateRequest itemReq : request.getItems()) {
            Ingredient ingredient = ingredientRepository.findById(itemReq.getIngredientId())
                    .orElseThrow(IngredientNotFoundException::new);

            PantryItem item = pantryItemMapper.toEntity(itemReq);
            item.setId(appUtil.generateUUID());
            item.setUser(userRepository.getReferenceById(userId));
            item.setIngredient(ingredient);

            itemsToSave.add(item);
        }

        List<PantryItem> savedItems = pantryItemRepository.saveAllAndFlush(itemsToSave);
        log.info("User {} batch added {} pantry items", userId, savedItems.size());
        return savedItems.stream()
                .map(pantryItemMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CursorResponse<PantryItemResponse> getMyItems(
            UUID cursor,
            int size,
            UUID categoryId,
            StorageLocation location,
            String keyword
    ) {
        UUID userId = appUtil.userIdFromAuthentication();
        int limit = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(0, limit + 1);

        String trimmedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        List<PantryItem> items = cursor == null
                ? pantryItemRepository.findItemsFirstPage(userId, categoryId, location, trimmedKeyword, pageable)
                : pantryItemRepository.findItemsNextPage(userId, cursor, categoryId, location, trimmedKeyword, pageable);

        return appUtil.buildCursorResponse(items, limit, PantryItem::getId, pantryItemMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PantryItemResponse getById(UUID id) {
        PantryItem item = findOwnedItem(id);
        return pantryItemMapper.toResponse(item);
    }

    @Transactional
    public PantryItemResponse update(UUID id, PantryItemUpdateRequest request) {
        PantryItem item = findOwnedItem(id);
        pantryItemMapper.updateEntity(request, item);
        item = pantryItemRepository.saveAndFlush(item);
        return pantryItemMapper.toResponse(item);
    }

    @Transactional
    public void delete(UUID id) {
        PantryItem item = findOwnedItem(id);
        pantryItemRepository.delete(item);
        log.info("User deleted pantry item {}", id);
    }

    @Transactional(readOnly = true)
    public List<PantryItemResponse> getExpiringItems(int days) {
        UUID userId = appUtil.userIdFromAuthentication();
        int effectiveDays = days > 0 ? days : 3;
        LocalDate thresholdDate = LocalDate.now().plusDays(effectiveDays);

        List<PantryItem> items = pantryItemRepository.findExpiringItems(userId, thresholdDate);
        return items.stream()
                .map(pantryItemMapper::toResponse)
                .toList();
    }

    @Transactional
    public PantryItemResponse uploadPhoto(UUID id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new org.group12.common.exception.AppException(ErrorCodePantry.EMPTY_IMAGE);
        }

        PantryItem item = findOwnedItem(id);
        UploadMediaResponse uploadResponse = mediaStorageService.uploadSync(file, "pantry-chef/pantry-items", MediaType.IMAGE);
        item.setPhotoUri(uploadResponse.getUrl());
        item = pantryItemRepository.saveAndFlush(item);

        log.info("Uploaded photo for pantry item {}", id);
        return pantryItemMapper.toResponse(item);
    }

    @Transactional
    public PantryItemResponse consume(UUID id, PantryItemConsumeRequest request) {
        PantryItem item = findOwnedItem(id);

        if (item.getQuantity() < request.getAmount()) {
            throw new InsufficientQuantityException();
        }

        double remaining = item.getQuantity() - request.getAmount();
        item.setQuantity(Math.max(remaining, 0.0));
        item = pantryItemRepository.saveAndFlush(item);

        log.info("Consumed {} from pantry item {}. Remaining: {}", request.getAmount(), id, item.getQuantity());
        return pantryItemMapper.toResponse(item);
    }

    private PantryItem findOwnedItem(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        return pantryItemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(PantryItemNotFoundException::new);
    }
}
