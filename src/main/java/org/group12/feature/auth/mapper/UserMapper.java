package org.group12.feature.auth.mapper;

import org.group12.feature.auth.dto.request.UserCreateRequest;
import org.group12.feature.auth.dto.request.UserUpdateRequest;
import org.group12.feature.auth.dto.response.UserResponse;
import org.group12.feature.auth.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);

    User toUser(UserCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(UserUpdateRequest req, @MappingTarget User user);

    @AfterMapping
    default void trimDisplayName(@MappingTarget User user) {
        if (user.getDisplayName() != null) {
            user.setDisplayName(user.getDisplayName().trim());
        }
    }
}
