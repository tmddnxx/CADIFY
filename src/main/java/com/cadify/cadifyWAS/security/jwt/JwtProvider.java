package com.cadify.cadifyWAS.security.jwt;

import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.security.common.LoginType;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {
    private final SecretKey secretKey;
    private final long expirationTimeAccessToken;
    @Getter
    private final long expirationTimeRefreshToken;

    public JwtProvider(@Value("${jwt.secret}") String keyString,
                       @Value("${jwt.expiration}") long expirationTimeAccessToken,
                       @Value("${jwt.expirationRefreshToken}") long expirationTimeRefreshToken) {
        this.expirationTimeAccessToken = expirationTimeAccessToken;
        byte[] byteKey = Base64.getDecoder().decode(keyString);
        this.secretKey = new SecretKeySpec(byteKey, SignatureAlgorithm.HS512.getJcaName());
        this.expirationTimeRefreshToken = expirationTimeRefreshToken;
    }

    // Access Token 생성
    public String generateAccessToken(String memberKey, LoginType loginType, MemberRole role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(memberKey)
                .claim("loginType", loginType.name())
                .claim("role", role.name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationTimeAccessToken)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }
    // Refresh Token 생성
    public String generateRefreshToken(String memberKey) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(memberKey)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationTimeRefreshToken)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    // token 검증 후 claims 추출 : 토큰 구조 검증, signature 검증, exp 만료시간 검증
    public Claims parseClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // header에서 token 추출
    public String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}