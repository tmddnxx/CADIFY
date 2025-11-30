package com.cadify.cadifyWAS.model.entity.member;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberAgreementType {
    TERMS_OF_SERVICE("서비스 이용약관 동의", true),
    PRIVACY_COLLECT("개인정보 수집 및 이용 동의", true),
    PRIVACY_POLICY("개인 정보 처리방침 확인", true),
    MARKETING_EMAIL("마케팅 이메일 수신 동의", false);

    // 설명
    private final String description;
    // 필수 여부
    private final boolean required;

    // 검증
    @JsonCreator
    public static MemberAgreementType fromString(String name){
        for(MemberAgreementType type : values()){
            if(type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new CustomLogicException(ExceptionCode.AGREEMENT_NOT_FOUND);
    }

}
