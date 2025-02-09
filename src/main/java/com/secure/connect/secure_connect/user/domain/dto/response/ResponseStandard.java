package com.secure.connect.secure_connect.user.domain.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Optional;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class ResponseStandard {

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private Optional<?> data;
}
