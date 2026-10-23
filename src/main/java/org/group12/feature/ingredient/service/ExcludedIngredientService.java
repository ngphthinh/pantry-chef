package org.group12.feature.ingredient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.ingredient.dto.request.ExcludedIngredientCreateRequest;
import org.group12.feature.ingredient.dto.response.ExcludedIngredientResponse;
import org.group12.feature.ingredient.entity.ExcludedIngredient;
import org.group12.feature.ingredient.exception.ExcludedIngredientDuplicateException;
import org.group12.feature.ingredient.exception.ExcludedIngredientNotFoundException;
import org.group12.feature.ingredient.mapper.ExcludedIngredientMapper;
import org.group12.feature.ingredient.repository.ExcludedIngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class ExcludedIngredientService {

    private final ExcludedIngredientRepository excludedIngredientRepository;
    private final UserRepository userRepository;
    private final ExcludedIngredientMapper excludedIngredientMapper;
    private final AppUtil appUtil;

    @Transactional(readOnly = true)
    public List<ExcludedIngredientResponse> getMyExcludedIngredients() {
        UUID userId = appUtil.userIdFromAuthentication();
        return excludedIngredientMapper.toExcludedIngredientResponses(
                excludedIngredientRepository.findAllByUserIdOrderByCreatedAtAsc(userId));
    }

    @Transactional
    public ExcludedIngredientResponse addExcludedIngredient(ExcludedIngredientCreateRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();
        String name = request.getName().trim();

        if (excludedIngredientRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new ExcludedIngredientDuplicateException();
        }

        ExcludedIngredient entity = ExcludedIngredient.builder()
                .id(appUtil.generateUUID())
                .name(name)
                .user(userRepository.getReferenceById(userId))
                .build();

        return excludedIngredientMapper.toExcludedIngredientResponse(excludedIngredientRepository.save(entity));
    }

    @Transactional
    public void removeExcludedIngredient(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        ExcludedIngredient entity = excludedIngredientRepository.findByIdAndUserId(id, userId)
                .orElseThrow(ExcludedIngredientNotFoundException::new);
        excludedIngredientRepository.delete(entity);
    }
}
