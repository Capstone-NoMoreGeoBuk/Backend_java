package com.back.domain.user.contorller;

import com.back.domain.user.dto.RefreshTokenResDto;
import com.back.domain.user.dto.UserMeResDto;
import com.back.domain.user.service.UserAuthService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "UserAuth", description = "사용자 인증 API")
@Slf4j
@RestController
@RequestMapping("/user/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserAuthService userAuthService;

    //400 Bad Request: 클라이언트가 잘못된 요청을 보냄 (형식 오류)
    //401 Unauthorized: 인증 실패 (토큰 없음/만료/유효하지 않음)
    //404 Not Found: 리소스를 찾을 수 없음
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "토큰이 유효하지 않거나 만료됨")
    })
    @PostMapping("/refresh")
    public RsData<RefreshTokenResDto> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        RefreshTokenResDto refreshTokenData = userAuthService.refreshTokens(request, response);

        if (refreshTokenData != null) {
            return RsData.of(200, "토큰이 갱신 성공.", refreshTokenData);
        } else {
            return RsData.of(401, "토큰 갱신에 실패했습니다. 다시 로그인해주세요.");
        }
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 종료하고 토큰을 무효화")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public RsData<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        userAuthService.logout(request, response);
        return RsData.of(200, "로그아웃되었습니다.");
    }

    @Operation(summary = "현재 로그인한 유저 정보 조회", description = "세션 유효성 검증 및 사용자 정보 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회"),
    })
    @GetMapping("/me")
    public RsData<UserMeResDto> getCurrentUser() {
        UserMeResDto userInfo = userAuthService.getCurrentUser();
        return RsData.of(200, "인증된 유저 정보 반환 성공", userInfo);
    }

    // OAuth 로그인 성공 시 리다이렉트되는 엔드포인트 (프론트 없이 테스트용)
    @GetMapping(value = "/login-success", produces = MediaType.TEXT_HTML_VALUE)
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserMeResDto userInfo = userAuthService.getCurrentUser();

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>OAuth 로그인 성공</title>
                <style>
                    body { font-family: Arial; max-width: 600px; margin: 50px auto; padding: 20px; }
                    .success { color: #28a745; font-size: 24px; margin-bottom: 20px; }
                    .info { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 10px 0; }
                    .label { font-weight: bold; color: #495057; }
                    .value { color: #212529; }
                    a { color: #007bff; text-decoration: none; }
                </style>
            </head>
            <body>
                <h1 class="success">✅ OAuth 로그인 성공!</h1>
                <div class="info">
                    <p><span class="label">사용자 ID:</span> <span class="value">%s</span></p>
                    <p><span class="label">이메일:</span> <span class="value">%s</span></p>
                    <p><span class="label">닉네임:</span> <span class="value">%s</span></p>
                    <p><span class="label">OAuth 제공자:</span> <span class="value">%s</span></p>
                </div>
                <p>✅ Access Token과 Refresh Token이 쿠키에 저장되었습니다.</p>
                <p><a href="/user/auth/me">📋 /user/auth/me 엔드포인트로 확인하기</a></p>
                <p><a href="/swagger-ui.html">📚 Swagger UI로 이동</a></p>
            </body>
            </html>
            """.formatted(
                userInfo.getUser() != null ? userInfo.getUser().getId() : "N/A",
                userInfo.getUser() != null ? userInfo.getUser().getEmail() : "N/A",
                userInfo.getUser() != null ? userInfo.getUser().getNickname() : "N/A",
                userInfo.getUser() != null ? userInfo.getUser().getProvider() : "N/A"
        );

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }

    // OAuth 로그인 실패 시 리다이렉트되는 엔드포인트 (프론트 없이 테스트용)
    @GetMapping(value = "/login-failure", produces = MediaType.TEXT_HTML_VALUE)
    public void loginFailure(@RequestParam(required = false) String message, HttpServletResponse response) throws IOException {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>OAuth 로그인 실패</title>
                <style>
                    body { font-family: Arial; max-width: 600px; margin: 50px auto; padding: 20px; }
                    .error { color: #dc3545; font-size: 24px; margin-bottom: 20px; }
                    .info { background: #f8d7da; padding: 15px; border-radius: 5px; margin: 10px 0; border: 1px solid #f5c6cb; }
                    .label { font-weight: bold; color: #721c24; }
                    .value { color: #721c24; }
                    a { color: #007bff; text-decoration: none; }
                </style>
            </head>
            <body>
                <h1 class="error">❌ OAuth 로그인 실패</h1>
                <div class="info">
                    <p><span class="label">에러 메시지:</span></p>
                    <p class="value">%s</p>
                </div>
                <p>💡 다시 시도하거나 백엔드 로그를 확인해주세요.</p>
                <p><a href="/oauth2/authorization/google">🔄 Google 로그인 재시도</a></p>
                <p><a href="/oauth2/authorization/kakao">🔄 Kakao 로그인 재시도</a></p>
                <p><a href="/oauth2/authorization/naver">🔄 Naver 로그인 재시도</a></p>
            </body>
            </html>
            """.formatted(message != null ? message : "알 수 없는 오류");

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }
}