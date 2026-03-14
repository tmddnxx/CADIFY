package com.cadify.cadifyWAS.model.entity;

import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.security.common.LoginType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenKey;  // 토큰 식별 키

    @Version
    private Long version;
    @Column(nullable = false)
    private String memberKey;   // 토큰 소유 회원 식별 키
    @Column(nullable = false, unique = true)
    private String refreshToken;    // 리프레시 토큰 string
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;    // 로그인 타입
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRole role;    // 사용자 권한

    private Instant createdAt;
    private Instant expiresAt;

    public void updateToken(String refreshToken, MemberRole role, Instant createdAt, Instant expiresAt){
        this.refreshToken = refreshToken;
        this.role = role;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}
