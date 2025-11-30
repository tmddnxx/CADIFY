package com.cadify.cadifyWAS.model.entity.member;

import com.cadify.cadifyWAS.model.dto.member.MemberDTO;
import com.cadify.cadifyWAS.util.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthMember extends BaseEntity {
    @Id
    private String memberKey;   // PK
    @Column(nullable = false)
    private String memberName;   // 이름
    @Column(nullable = false, unique = true)
    private String email;   // 이메일
    @Column(unique = true)
    private String phone;   // 전화번호
    @Column
    private Integer addressNumber;  // 주소 ( 필요? )
    @Column
    private String addressDetail;   // 상세주소 ( 필요? )
    @Column
    @Enumerated(EnumType.STRING)
    private MemberRole role; // 권한
    @Column
    private String provider;    // OAuth 인증 제공사
    @Column
    private String authKey;  // 인증 제공사가 보장하는 유저 고유 값

    public OAuthMember insertPhone(String phone) {
        this.phone = phone;
        return this;
    }

    // 사용자 정보 수정
    public OAuthMember update(MemberDTO.UpdateMember fields) {
        Optional.ofNullable(fields.getMemberName())
                .ifPresent(name -> this.memberName = name);
        return this;
    }

    public OAuthMember updateForCompany(String companyName){
        Optional.ofNullable(companyName).ifPresent(name -> this.memberName = companyName);
        return this;
    }

    // Company 전환
    public OAuthMember convertToCompany(String companyName){
        Optional.ofNullable(companyName).ifPresent(name -> this.memberName = companyName);
        this.role = MemberRole.COMPANY;
        return this;
    }

    public OAuthMember assignRoleForFirstLogin(MemberRole role){
        this.role = role;
        return this;
    }
}
