package com.secure.connect.secure_connect.user.domain.mapper;

import com.secure.connect.secure_connect.user.domain.User;
import com.secure.connect.secure_connect.user.domain.dto.request.UserRequest;
import com.secure.connect.secure_connect.user.domain.dto.response.UserResponse;

public class UserMapper {

    public static User userRequestToUser(UserRequest userRequest) {
        return User.builder()
                .name(userRequest.getName())
                .password(userRequest.getPassword())
                .role(userRequest.getRole())
                .build();
    }

    public static UserResponse userToUserResponse(User user) {
        return UserResponse.builder()
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
