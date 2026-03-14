package com.cadify.cadifyWAS.model.entity.member;

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

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String memberKey;   // 사용자 식별 키

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberAgreementType memberAgreementType;  // 약관 종류

    @Column(nullable = false)
    private boolean agreed;     // 동의 여부

    @Column(nullable = false)
    private LocalDateTime agreedAt; // 동의
}
