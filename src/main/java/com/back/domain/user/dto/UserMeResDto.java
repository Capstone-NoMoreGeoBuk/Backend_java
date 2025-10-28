package com.back.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMeResDto {
    @JsonProperty("user")
    private final UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private final String id;
        private final String email;
        private final String nickname;
        private final String provider;
    }
}
