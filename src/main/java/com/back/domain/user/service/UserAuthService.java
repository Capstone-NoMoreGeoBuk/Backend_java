package com.back.domain.user.service;

import com.back.domain.user.dto.RefreshTokenResDto;
import com.back.domain.user.dto.UserMeResDto;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.jwt.JwtUtil;
import com.back.global.jwt.refreshToken.entity.RefreshToken;
import com.back.global.jwt.refreshToken.repository.RefreshTokenRepository;
import com.back.global.jwt.refreshToken.service.RefreshTokenService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.security.core.context.SecurityContextHolder.clearContext;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Rq rq;

    //OAuth 관련

    public User joinSocial(String oauthId, String email, String nickname){
        userRepository.findByOauthId(oauthId)
                .ifPresent(user -> {
                    throw new ServiceException(409, "이미 존재하는 계정입니다.");
                });

        User user = User.builder()
                .email(email != null ? email : "")
                .nickname(nickname)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .oauthId(oauthId)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public RsData<User> findOrCreateOAuthUser(String oauthId, String email, String nickname) {
        Optional<User> existingUser = userRepository.findByOauthId(oauthId);

        if (existingUser.isPresent()) {
            // 기존 사용자 업데이트 (이메일만 업데이트)
            User user = existingUser.get();
            // null 체크 후 빈 문자열로 대체
            user.setEmail(email != null ? email : "");
            return RsData.of(200, "회원 정보가 업데이트 되었습니다", user); //더티체킹
        } else {
            User newUser = joinSocial(oauthId, email, nickname);
            return RsData.of(201, "사용자가 생성되었습니다", newUser);
        }
    }

    // 리프레시 토큰 관련

    public void issueTokens(HttpServletResponse response, Long userId, String email, String nickname) {
        String accessToken = jwtUtil.generateAccessToken(userId, email, nickname);
        String refreshToken = refreshTokenService.generateRefreshToken(userId);

        log.debug("토큰 발급 완료 - userId: {}, accessToken: {}, refreshToken: {}", userId, accessToken, refreshToken);

        jwtUtil.addAccessTokenToCookie(response, accessToken);
        jwtUtil.addRefreshTokenToCookie(response, refreshToken);
    }

    public RefreshTokenResDto refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        try {
            String oldRefreshToken = jwtUtil.getRefreshTokenFromCookie(request);
            log.debug("토큰 갱신 시도 - 받은 RefreshToken: {}", oldRefreshToken);

            if (oldRefreshToken == null) {
                log.error("RefreshToken이 쿠키에서 발견되지 않음");
                return null;
            }

            if (!refreshTokenService.validateToken(oldRefreshToken)) {
                log.error("RefreshToken 검증 실패: {}", oldRefreshToken);
                return null;
            }

            Optional<RefreshToken> tokenData = refreshTokenRepository.findByToken(oldRefreshToken);
            if (tokenData.isEmpty()) {
                return null;
            }

            RefreshToken refreshTokenEntity = tokenData.get();
            Long userId = refreshTokenEntity.getUserId();

            // DB에서 사용자 정보 조회
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return null;
            }

            User user = userOpt.get();

            String newRefreshToken = refreshTokenService.rotateToken(oldRefreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(userId, user.getEmail(), user.getNickname());

            jwtUtil.addAccessTokenToCookie(response, newAccessToken);
            jwtUtil.addRefreshTokenToCookie(response, newRefreshToken);

            return RefreshTokenResDto.builder()
                    .accessToken(newAccessToken)
                    .user(
                            RefreshTokenResDto.UserInfoDto.builder()
                                    .id(user.getId().toString())
                                    .nickname(user.getNickname())
                                    .build()
                    )
                    .build();
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    //토큰 끊기면서 OAuth 자동 로그아웃
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. RefreshToken DB에서 삭제
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }

        // 2. JWT 쿠키 삭제
        jwtUtil.removeAccessTokenCookie(response);
        jwtUtil.removeRefreshTokenCookie(response);

        // 3. Spring Security 세션 무효화 (Redis 포함)
        try {
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
                log.debug("세션 무효화");
            }
        } catch (IllegalStateException e) {
            log.debug("세션이 이미 무효화되어 있음");
        }

        // 4. SecurityContext 클리어
        clearContext();

        log.info("로그아웃 완료 - JWT, 세션, SecurityContext 모두 정리됨");
    }

    // 현재 로그인한 사용자 정보 조회 (세션 검증용)
    // 변경: 항상 200 응답, 비로그인 시 user: null 반환
    public UserMeResDto getCurrentUser() {
        try {
            User actor = rq.getActor();

            // 비로그인 상태: user null 반환
            if (actor == null) {
                log.debug("인증되지 않은 사용자 - user: null 반환");
                return UserMeResDto.builder()
                        .user(null)
                        .build();
            }

            Optional<User> userOpt = userRepository.findById(actor.getId());
            if (userOpt.isEmpty()) {
                log.warn("사용자 ID {}를 DB에서 찾을 수 없음 (토큰은 유효하나 사용자 삭제됨)", actor.getId());
                return UserMeResDto.builder()
                        .user(null)
                        .build();
            }

            // 로그인 상태: user 정보 반환
            User user = userOpt.get();
            String provider = extractProvider(user.getOauthId());

            return UserMeResDto.builder()
                    .user(UserMeResDto.UserInfo.builder()
                            .id(user.getId().toString())
                            .email(user.getEmail())
                            .nickname(user.getNickname())
                            .provider(provider)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("사용자 정보 조회 중 서버 오류 발생: {}", e.getMessage(), e);
            // 예외 발생 시에도 user: null 반환
            return UserMeResDto.builder()
                    .user(null)
                    .build();
        }
    }

    private String extractProvider(String oauthId) {
        if (oauthId == null || oauthId.isBlank()) {
            return "unknown";
        }
        String[] parts = oauthId.split("_", 2);
        return parts.length > 0 ? parts[0] : "unknown";
    }
}