package com.back.global.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class SecurityUser extends User implements OAuth2User {
    @Getter
    private UUID id;

    @Getter
    private String nickname;

    @Getter
    private String email;

    private Map<String, Object> attributes;

    // OAuth2 전용 생성자 (패스워드 없음)
    public SecurityUser(
            UUID id,
            String email,
            String nickname,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes
    ) {
        super(
                (email != null && !email.isBlank()) ? email : id.toString(), // ★★★ 핵심 수정
                "",
                authorities
        );

        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return nickname; // OAuth2User 인터페이스용 - nickname 반환
    }

    public String getNickname() {
        return getName();
    }
}
