package com.cadify.cadifyWAS.model.entity.factory;

import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.util.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactoryAdmin extends BaseEntity {

    @Id
    private String memberKey;   // 회원 공통 식별키

    @Column(unique = true, nullable = false)
    private String username;    // 관리자 로그인 아이디

    @Column(unique = true, nullable = false)
    private String password;    // 관리자 로그인 비밀번호

    @Column
    private String name;  // 관리자 이름

    @Column(nullable = false, unique = true)
    private String email;   // 이메일

    @Column(unique = true, nullable = false)
    private String phone;   // 관리자 개인 전화번호

    @Column
    @Enumerated(EnumType.STRING)
    private MemberRole role;    // 권한 식별

    @Column(nullable = false)
    private String factoryKey;  //  소속 공장 식별 키
}