package com.cadify.cadifyWAS.repository.member;

import com.cadify.cadifyWAS.model.dto.member.agreement.MemberAgreementResponse;
import com.cadify.cadifyWAS.model.entity.member.MemberAgreementType;

import java.util.List;

public interface MemberAgreementQueryRepository {
    // 사용자 필수 동의 여부 반환
    List<MemberAgreementResponse> getPersonalAgreements(String memberKey, List<MemberAgreementType> agreements);
}
