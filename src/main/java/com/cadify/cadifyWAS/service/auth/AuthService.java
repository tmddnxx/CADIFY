package com.cadify.cadifyWAS.service.auth;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.dto.auth.AuthDTO;
import com.cadify.cadifyWAS.model.entity.Token;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.repository.token.JwtRepository;
import com.cadify.cadifyWAS.security.common.LoginType;
import com.cadify.cadifyWAS.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

// FORM, OAuth2.0 공통 비즈니스 로직
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtRepository jwtRepository;
    private final JwtProvider jwtProvider;

    // 로그아웃: refresh token 삭제.
    @Transactional
    public void logoutProcess(AuthDTO.RefreshTokenInfo request) {
        int deletedRows = jwtRepository.deleteByRefreshToken(request.getRefreshToken());
        if (deletedRows < 1) {
            throw new CustomLogicException(ExceptionCode.USER_ALREADY_LOGOUT);
        }
    }

    // refresh token 사용해서 엑세스 토큰 발급 및 재발급
    @Transactional
    public String refreshProcess(String refreshToken){
        try{
            // 검증 후 claims 가져오기
            Claims claims = jwtProvider.parseClaims(refreshToken);
            String memberKey = claims.getSubject();
            Instant now = Instant.now();

            // member key 로 refresh token 조회 -> 없는 경우: 로그아웃, 회원 탈퇴
            Optional<Token> optionalToken = jwtRepository.findByMemberKey(memberKey);
            Token existToken = optionalToken.
                    orElseThrow(() -> new CustomLogicException(ExceptionCode.MEMBER_NOT_FOUND));

            // 새로운 refresh token 으로 업데이트
            existToken.updateToken(memberKey, existToken.getRole(), now, now.plusMillis(jwtProvider.getExpirationTimeRefreshToken()));
            // 업데이트한 토큰 저장
            jwtRepository.save(existToken);

            // 새로운 Access token 반환
            return jwtProvider.generateAccessToken(existToken.getMemberKey(), existToken.getLoginType(), existToken.getRole());

        } catch (ExpiredJwtException e) {
            // 만료된 토큰일 경우
            log.info("Refresh Token 만료됨: {}", e.getMessage());
            throw new CustomLogicException(ExceptionCode.EXPIRED_REFRESH_TOKEN);

        } catch (JwtException e) {
            // 유효하지 않은 토큰일 경우, 검증 실패
            log.warn("Refresh Token 검증 실패: {}", e.getMessage());
            throw new CustomLogicException(ExceptionCode.INVALID_REFRESH_TOKEN);
        }
    }

    // ( FORM, OAuth2.0 공용 ), 로그인시  refreshToken 생성.
    public String generateRefreshTokenForLogin(String memberKey, LoginType loginType, MemberRole memberRole) {
        Instant now = Instant.now();

        // 새 Refresh Token 생성
        String refreshToken = jwtProvider.generateRefreshToken(memberKey);

        // 기존 토큰 존재 여부 확인
        Optional<Token> optionalToken = jwtRepository.findByMemberKey(memberKey);

        if (optionalToken.isPresent()) {
            // 토큰이 존재하는 경우: 새로운 토큰 생성 후 업데이트
            Token existing = optionalToken.get();
            existing.updateToken(refreshToken, memberRole, now, now.plusMillis(jwtProvider.getExpirationTimeRefreshToken()));
            jwtRepository.save(existing);
        } else {
            // 존재하지 않는 경우 : 새로운 토큰 생성 후 저장
            Token newToken = Token.builder()
                    .memberKey(memberKey)
                    .refreshToken(refreshToken)
                    .loginType(loginType)
                    .role(memberRole)
                    .createdAt(now)
                    .expiresAt(now.plusMillis(jwtProvider.getExpirationTimeRefreshToken()))
                    .build();
            jwtRepository.save(newToken);
        }

        return refreshToken;
    }
}