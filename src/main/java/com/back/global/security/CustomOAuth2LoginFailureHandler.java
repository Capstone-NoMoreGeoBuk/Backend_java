package com.back.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomOAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Value("${custom.site.frontUrl}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 로그인 실패: {}", exception.getMessage());

        // 프론트엔드 에러 페이지로 리다이렉트 (실제 배포시 사용)
        // String redirectUrl = frontendUrl + "/oauth/error?message=" + exception.getMessage();

        // 프론트엔드 없이 테스트할 수 있도록 백엔드로 리다이렉트
        String redirectUrl = "/api/auth/login-failure?message=" + exception.getMessage();

        response.sendRedirect(redirectUrl);
    }
}