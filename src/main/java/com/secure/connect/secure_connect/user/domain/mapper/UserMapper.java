package com.secure.connect.secure_connect.user.domain.mapper;

import com.secure.connect.secure_connect.user.domain.User;
import com.secure.connect.secure_connect.user.domain.dto.request.UserRequest;
import com.secure.connect.secure_connect.user.domain.dto.response.UserResponse;

import java.util.ArrayList;
import java.util.List;

public class UserMapper {

    public static User userRequestToUser(UserRequest userRequest) {
        return User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .role(userRequest.getRole())
                .build();
    }

    public static UserResponse userToUserResponse(User user) {
       return UserResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .userName(user.getUsername())
                .build();
    }

    public static List<UserResponse> userListToUserResponse(List<User> lisUsers) {
        List<UserResponse> list = new ArrayList<>();

        for (User user : lisUsers) {
            list.add(UserResponse.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .userName(user.getUsername())
                    .build());
        }

        return list;
    }
}
