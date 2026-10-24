package org.group12.feature.pantry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.dto.response.CursorResponse;
import org.group12.common.dto.response.UploadMediaResponse;
import org.group12.common.enums.MediaType;
import org.group12.common.exception.AppException;
import org.group12.common.service.MediaStorageService;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.entity.User;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.ingredient.entity.Ingredient;
import org.group12.feature.ingredient.exception.IngredientNotFoundException;
import org.group12.feature.ingredient.repository.IngredientRepository;
import org.group12.feature.pantry.dto.request.*;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class PantryItemService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final String PHOTO_FOLDER = "pantry-chef/pantry-items";

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

        PantryItem item = buildItem(request, userRepository.getReferenceById(userId), ingredient);
        item = pantryItemRepository.saveAndFlush(item);

        log.info("User {} added pantry item {}", userId, item.getId());
        return pantryItemMapper.toResponse(item);
    }

    @Transactional
    public List<PantryItemResponse> createBatch(PantryItemBatchCreateRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();
        User user = userRepository.getReferenceById(userId);
        Map<UUID, Ingredient> ingredients = loadIngredients(request.getItems());

        List<PantryItem> items = request.getItems().stream()
                .map(req -> buildItem(req, user, ingredients.get(req.getIngredientId())))
                .toList();

        List<PantryItem> saved = pantryItemRepository.saveAllAndFlush(items);
        log.info("User {} batch added {} pantry items", userId, saved.size());
        return toResponses(saved);
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
        int limit = clampPageSize(size);
        Pageable pageable = PageRequest.of(0, limit + 1);
        String normalizedKeyword = normalizeKeyword(keyword);

        List<PantryItem> items = cursor == null
                ? pantryItemRepository.findItemsFirstPage(userId, categoryId, location, normalizedKeyword, pageable)
                : pantryItemRepository.findItemsNextPage(userId, cursor, categoryId, location, normalizedKeyword, pageable);

        return appUtil.buildCursorResponse(items, limit, PantryItem::getId, pantryItemMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PantryItemResponse getById(UUID id) {
        return pantryItemMapper.toResponse(findOwnedItem(id));
    }

    @Transactional
    public PantryItemResponse update(UUID id, PantryItemUpdateRequest request) {
        PantryItem item = findOwnedItem(id);
        pantryItemMapper.updateEntity(request, item);
        return pantryItemMapper.toResponse(pantryItemRepository.saveAndFlush(item));
    }

    @Transactional
    public void delete(UUID id) {
        PantryItem item = findOwnedItem(id);
        pantryItemRepository.delete(item);
        log.info("Deleted pantry item {}", id);
    }

    @Transactional(readOnly = true)
    public List<PantryItemResponse> getExpiringItems(int days) {
        UUID userId = appUtil.userIdFromAuthentication();
        LocalDate threshold = LocalDate.now().plusDays(days);
        return toResponses(pantryItemRepository.findExpiringItems(userId, threshold));
    }

    @Transactional
    public PantryItemResponse uploadPhoto(UUID id, ImageUploadRequest file) {

        PantryItem item = findOwnedItem(id);

        if (item.getPublicImgId() != null) {
            mediaStorageService.delete(item.getPublicImgId()); // delete old photo if exists
        }

        UploadMediaResponse upload = mediaStorageService.uploadSync(file.getImage(), PHOTO_FOLDER, MediaType.IMAGE);
        item.setPhotoUri(upload.getUrl());
        item.setPublicImgId(upload.getPublicId());
        item = pantryItemRepository.saveAndFlush(item);

        log.info("Uploaded photo for pantry item {}", id);
        return pantryItemMapper.toResponse(item);
    }

    @Transactional
    public PantryItemResponse consume(UUID id, PantryItemConsumeRequest request) {
        PantryItem item = findOwnedItem(id);
        double consumed = request.getQuantity();

        if (item.getQuantity() < consumed) {
            throw new InsufficientQuantityException();
        }

        item.setQuantity(item.getQuantity() - consumed);
        item = pantryItemRepository.saveAndFlush(item);

        log.info("Consumed {} from pantry item {}. Remaining: {}", consumed, id, item.getQuantity());
        return pantryItemMapper.toResponse(item);
    }

    // ---------- private helpers ----------

    private PantryItem findOwnedItem(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        return pantryItemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(PantryItemNotFoundException::new);
    }

    private PantryItem buildItem(PantryItemCreateRequest request, User user, Ingredient ingredient) {
        PantryItem item = pantryItemMapper.toEntity(request);
        item.setId(appUtil.generateUUID());
        item.setUser(user);
        item.setIngredient(ingredient);
        return item;
    }

    /** Load all ingredients in one query; fail if any id is missing. */
    private Map<UUID, Ingredient> loadIngredients(List<PantryItemCreateRequest> requests) {
        Set<UUID> ids = requests.stream()
                .map(PantryItemCreateRequest::getIngredientId)
                .collect(Collectors.toSet());

        Map<UUID, Ingredient> found = ingredientRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Ingredient::getId, Function.identity()));

        if (found.size() != ids.size()) {
            throw new IngredientNotFoundException();
        }
        return found;
    }

    private List<PantryItemResponse> toResponses(List<PantryItem> items) {
        return items.stream().map(pantryItemMapper::toResponse).toList();
    }

    private int clampPageSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private String normalizeKeyword(String keyword) {
        return (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    }
}