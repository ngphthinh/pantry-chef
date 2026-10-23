package org.group12.feature.auth.service;

import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.CursorResponse;
import org.group12.common.dto.response.UploadMediaResponse;
import org.group12.common.exception.UploadMediaException;
import org.group12.common.service.impl.CloudinaryMediaStorageService;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.dto.request.UserUpdateRequest;
import org.group12.feature.auth.dto.response.UserResponse;
import org.group12.feature.auth.entity.User;
import org.group12.feature.auth.exception.UserNotFoundException;
import org.group12.feature.auth.mapper.UserMapper;
import org.group12.feature.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AppUtil appUtil;
    private final CloudinaryMediaStorageService cloudinaryMediaStorageService;

    @Value("${app.avatar.folder}")
    private String FOLDER_AVATAR;

    @Transactional(readOnly = true)
    public UserResponse getProfile() {
        UUID userId = appUtil.userIdFromAuthentication();
        return userMapper.toUserResponse(findUserOrThrow(userId));
    }

    @Transactional
    public UserResponse updateProfile(UserUpdateRequest req) {
        UUID userId = appUtil.userIdFromAuthentication();
        User user = findUserOrThrow(userId);
        userMapper.updateUser(req, user);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public void deleteAccount() {
        UUID userId = appUtil.userIdFromAuthentication();
        User user = findUserOrThrow(userId);
        if (user.getPublicAvtId() != null) {
            cloudinaryMediaStorageService.delete(user.getPublicAvtId());
        }
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse updateAvatar(MultipartFile multipartFile) {
        UUID userId = appUtil.userIdFromAuthentication();
        User user = findUserOrThrow(userId);
        try {
            UploadMediaResponse uploadMediaResponse = cloudinaryMediaStorageService.uploadImage(multipartFile.getBytes(), FOLDER_AVATAR + "/" + userId).join();

            cloudinaryMediaStorageService.delete(user.getPublicAvtId());

            user.setAvatarUri(uploadMediaResponse.getUrl());
            user.setPublicAvtId(uploadMediaResponse.getPublicId());
        } catch (IOException e) {
            throw new UploadMediaException();
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }


    @Transactional(readOnly = true)
    public CursorResponse<UserResponse> getAllUsers(UUID cursor, int size, String keyword) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        List<User> rows = userRepository.findPage(cursor, kw, PageRequest.of(0, size + 1));

        return appUtil.buildCursorResponse(rows, size, User::getId, userMapper::toUserResponse);
    }
}