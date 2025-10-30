package com.back.global.security;

import com.back.domain.user.service.UserAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserAuthService userAuthService;

    @Value("${custom.site.frontUrl}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("========== OAuth2 로그인 성공 핸들러 실행 ==========");

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        log.info("사용자 정보 - ID: {}, Email: {}, Nickname: {}",
                securityUser.getId(), securityUser.getEmail(), securityUser.getNickname());

        // Access Token과 Refresh Token 발급
        try {
            userAuthService.issueTokens(response, securityUser.getId(), securityUser.getEmail(), securityUser.getNickname());
            log.info("토큰 발급 성공");
        } catch (Exception e) {
            log.error("토큰 발급 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }

        // 실제 배포시 사용
        // response.sendRedirect(frontendUrl + "/login/user/success");

        // 프론트엔드 없이 테스트할 수 있도록 백엔드로 리다이렉트
        String redirectUrl = "/user/auth/login-success";
        log.info("리다이렉트 URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
        log.info("========== OAuth2 로그인 성공 핸들러 완료 ==========");
    }
}