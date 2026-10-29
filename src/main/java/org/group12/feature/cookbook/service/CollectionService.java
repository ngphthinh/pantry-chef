package org.group12.feature.cookbook.service;

import lombok.RequiredArgsConstructor;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.cookbook.dto.request.CollectionCreateRequest;
import org.group12.feature.cookbook.dto.request.CollectionUpdateRequest;
import org.group12.feature.cookbook.dto.response.CollectionResponse;
import org.group12.feature.cookbook.dto.response.CollectionResponseDetail;
import org.group12.feature.cookbook.entity.Collection;
import org.group12.feature.cookbook.entity.SavedRecipe;
import org.group12.feature.cookbook.exception.CollectionNameDuplicateException;
import org.group12.feature.cookbook.exception.CollectionNotFoundException;
import org.group12.feature.cookbook.exception.SavedRecipeNotFoundException;
import org.group12.feature.cookbook.mapper.CollectionMapper;
import org.group12.feature.cookbook.repository.CollectionRepository;
import org.group12.feature.cookbook.repository.SavedRecipeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@PreAuthorize("hasRole('USER')")
@Service
@RequiredArgsConstructor
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final AppUtil appUtil;
    private final UserRepository userRepository;
    private final SavedRecipeRepository savedRecipeRepository;
    private final CollectionMapper collectionMapper;


    @Transactional(readOnly = true)
    public List<CollectionResponse> list() {
        UUID userId = appUtil.userIdFromAuthentication();
        return collectionRepository.findAllByUser(userId);
    }

    @Transactional
    public CollectionResponse create(CollectionCreateRequest req) {
        UUID userId = appUtil.userIdFromAuthentication();
        String name = req.getName().trim();

        if (collectionRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new CollectionNameDuplicateException();
        }

        Collection collection = Collection.builder()
                .id(appUtil.generateUUID())
                .name(name)
                .user(userRepository.getReferenceById(userId))
                .build();

        return collectionMapper.toCollectionResponse(collectionRepository.save(collection), 0);
    }

    @Transactional
    public CollectionResponse update(UUID id, CollectionUpdateRequest req) {
        UUID userId = appUtil.userIdFromAuthentication();
        Collection collection = collectionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(CollectionNotFoundException::new);

        String name = req.getName().trim();
        if (collectionRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, name, id)) {
            throw new CollectionNameDuplicateException();
        }

        collection.setName(name);
        collectionRepository.save(collection);

        return collectionMapper.toCollectionResponse(collection, (int) collectionRepository.countRecipes(id));
    }

    @Transactional
    public void delete(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        Collection collection = collectionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(CollectionNotFoundException::new);
        collectionRepository.delete(collection);
    }

    @Transactional
    public CollectionResponse addRecipe(UUID id, UUID savedRecipeId) {
        UUID userId = appUtil.userIdFromAuthentication();

        Collection collection = collectionRepository.findWithRecipesByIdAndUserId(id, userId)
                .orElseThrow(CollectionNotFoundException::new);
        SavedRecipe savedRecipe = savedRecipeRepository.findByIdAndUserId(savedRecipeId, userId)
                .orElseThrow(SavedRecipeNotFoundException::new);

        collection.getSavedRecipes().add(savedRecipe);
        collectionRepository.save(collection);

        return collectionMapper.toCollectionResponse(collection, (int) collectionRepository.countRecipes(id));
    }

    @Transactional
    public CollectionResponse removeRecipe(UUID id, UUID savedRecipeId) {
        UUID userId = appUtil.userIdFromAuthentication();

        Collection collection = collectionRepository.findWithRecipesByIdAndUserId(id, userId)
                .orElseThrow(CollectionNotFoundException::new);

        collection.getSavedRecipes().removeIf(sr -> sr.getId().equals(savedRecipeId));
        collectionRepository.save(collection);

        return collectionMapper.toCollectionResponse(collection, (int) collectionRepository.countRecipes(id));
    }


    @Transactional(readOnly = true)
    public CollectionResponseDetail get(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        Collection collection = collectionRepository.findCollectionByIdAndUserId(id, userId).orElseThrow(CollectionNotFoundException::new);
        return collectionMapper.toCollectionResponseDetail(collection, (int) collectionRepository.countRecipes(id));

    }
}
