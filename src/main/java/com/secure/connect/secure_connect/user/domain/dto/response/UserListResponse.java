package com.secure.connect.secure_connect.user.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.secure.connect.secure_connect.user.domain.User;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserListResponse {

    @JsonProperty("users")
    private List<UserResponse> userList;
}
