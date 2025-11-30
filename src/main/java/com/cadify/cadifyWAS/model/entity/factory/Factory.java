package com.cadify.cadifyWAS.model.entity.factory;

import com.cadify.cadifyWAS.util.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Factory extends BaseEntity {

    @Id
    private String factoryKey;  // 식별 키

    @Column(nullable = false, unique = true)
    private String factoryName; // 공장 이름

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FactoryType factoryType; // 공장 유형 ( 담당하는 가공 유형 구분 )

    @Column(nullable = false)
    private String businessCode;    // 사업자 번호

    @Column(nullable = false)
    private String addressNumber;   // 공장 우편번호

    @Column
    private String addressDetail;   // 공장 주소 세부사항

    @Column(nullable = false)
    private String owner;   // 사업자

    @Column(nullable = false)
    private String ownerPhone;  // 사업자 개인 전화번호

    @Column
    private String officePhone; // 공장 전화번호

    @Column(nullable = false)
    private String bank;    // 은행 이름

    @Column(nullable = false)
    private String account; // 계좌 번호
}
