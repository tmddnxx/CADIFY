package com.cadify.cadifyWAS.model.entity.member;

import jakarta.persistence.*;
import lombok.*;

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
