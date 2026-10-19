package org.group12.feature.auth.mapper;

import org.group12.feature.auth.dto.request.UserCreateRequest;
import org.group12.feature.auth.dto.response.UserResponse;
import org.group12.feature.auth.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);

    User toUser(UserCreateRequest request);
}
