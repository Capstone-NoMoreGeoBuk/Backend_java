package com.back.global.security;

import com.back.domain.user.entity.User;
import com.back.domain.user.service.UserAuthService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserAuthService userAuthService;

    // OAuth2 로그인 성공 시 자동 호출
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("========== CustomOAuth2UserService.loadUser 시작 ==========");

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2User 로드 완료");

        String oauthUserId = "";
        String providerTypeCode = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        String nickname = "";
        String email = "";

        switch (providerTypeCode) {
            case "KAKAO" -> {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                Map<String, Object> attributesProperties = (Map<String, Object>) attributes.get("properties");

                oauthUserId = oAuth2User.getName();
                nickname = (String) attributesProperties.get("nickname");
            }
            case "GOOGLE" -> {
                oauthUserId = oAuth2User.getName();
                nickname = (String) oAuth2User.getAttributes().get("name");
                email = (String) oAuth2User.getAttributes().get("email");
            }
            case "NAVER" -> {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                Map<String, Object> attributesProperties = (Map<String, Object>) attributes.get("response");

                oauthUserId = (String) attributesProperties.get("id");
                nickname = (String) attributesProperties.get("nickname");
                email = (String) attributesProperties.get("email");
            }
        }

        // OAuth ID를 제공자와 함께 저장 (예: kakao_123456789)
        String uniqueOauthId = providerTypeCode.toLowerCase() + "_" + oauthUserId;
        log.info("OAuth 제공자: {}, uniqueOauthId: {}, email: {}, nickname: {}",
                providerTypeCode, uniqueOauthId, email, nickname);

        RsData<User> rsData = userAuthService.findOrCreateOAuthUser(uniqueOauthId, email, nickname);
        log.info("사용자 생성/조회 결과 - code: {}, message: {}", rsData.code(), rsData.message());

        if (rsData.code()<200 || rsData.code()>299) {
            log.error("사용자 생성/조회 실패");
            throw new OAuth2AuthenticationException("사용자 생성/조회 실패: " + rsData.message());
        }

        User user = rsData.data();
        log.info("User 엔티티 - ID: {}, Email: {}, Nickname: {}, Role: {}",
                user.getId(), user.getEmail(), user.getNickname(), user.getRole());

        String userEmail = user.getEmail() != null && !user.getEmail().trim().isEmpty()
                ? user.getEmail() : "unknown";

        log.info("SecurityUser 생성 시작 - Authorities 개수: {}", user.getAuthorities().size());
        user.getAuthorities().forEach(auth -> log.info("Authority: {}", auth.getAuthority()));

        SecurityUser securityUser = new SecurityUser(
                user.getId(),
                userEmail,
                user.getNickname(),
                user.getAuthorities(),
                oAuth2User.getAttributes()
        );

        log.info("========== CustomOAuth2UserService.loadUser 완료 ==========");
        return securityUser;
    }
}
