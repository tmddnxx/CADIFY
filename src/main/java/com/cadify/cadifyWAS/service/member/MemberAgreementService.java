package com.cadify.cadifyWAS.service.member;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.dto.member.agreement.MemberAgreementDTO;
import com.cadify.cadifyWAS.model.dto.member.agreement.MemberAgreementResponse;
import com.cadify.cadifyWAS.model.entity.member.MemberAgreement;
import com.cadify.cadifyWAS.model.entity.member.MemberAgreementType;
import com.cadify.cadifyWAS.model.entity.member.OAuthMember;
import com.cadify.cadifyWAS.repository.member.MemberAgreementRepository;
import com.cadify.cadifyWAS.repository.member.OAuthMemberRepository;
import com.cadify.cadifyWAS.security.jwt.JwtPrincipal;
import com.cadify.cadifyWAS.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberAgreementService {
    private final OAuthMemberRepository memberRepository;
    private final MemberAgreementRepository agreementRepository;
    private final JwtUtil jwtUtil;

    // 동의 메서드
    @Transactional
    public void agreementProcess(List<MemberAgreementDTO.AgreementRequest> requests){
        String memberKey = isValidMember();

        for(MemberAgreementDTO.AgreementRequest request : requests){
            MemberAgreement agreement = MemberAgreement.builder()
                    .memberKey(memberKey)
                    .memberAgreementType(request.getAgreementType())
                    .agreed(request.getAgreed())
                    .agreedAt(LocalDateTime.now())
                    .build();
            // 저장
            agreementRepository.save(agreement);
        }
    }

    // 최신 필수 동의 여부 조회
    @Transactional(readOnly = true)
    public List<MemberAgreementResponse> getPersonalRequiredAgreedList(){
        String memberKey = isValidMember();
        return isAgreedRequiredTerms(memberKey);
    }

    // 이메일 전송 메서드
    // null

    // 최신 마케팅 동의 여부 조회
    @Transactional(readOnly = true)
    public List<MemberAgreementResponse> getPersonalMarketingAgreedList(){
        String memberKey = isValidMember();
        return isAgreedMarketingTerms(memberKey);
    }




// 내부 메서드
    // 멤버키 확인, 탈퇴 여부 확인
    private String isValidMember(){
        JwtPrincipal jwtPrincipal = jwtUtil.getAuthPrincipalObject();
        // 존재 검증
        OAuthMember member = memberRepository.findByMemberKeyAndDeletedFalse(jwtPrincipal.getMemberKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        // 탈퇴 검증
        if(member.getDeleted() == true){
            throw new CustomLogicException(ExceptionCode.WITHDRAWN_MEMBER);
        }
        return jwtPrincipal.getMemberKey();
    }

    // 필수 동의 여부 확인
    public List<MemberAgreementResponse> isAgreedRequiredTerms(String memberKey){
        // 필수 동의 타입
        List<MemberAgreementType> requiredTypes = Arrays.stream(MemberAgreementType.values())
                .filter(MemberAgreementType::isRequired)
                .toList();

        List<MemberAgreementResponse> result = agreementRepository.getPersonalAgreements(memberKey, requiredTypes);
        if(result.isEmpty()){
            throw new CustomLogicException(ExceptionCode.NO_AGREED_TERMS);
        }

        return result;
    }

    // 마케팅 동의 여부 확인
    public List<MemberAgreementResponse> isAgreedMarketingTerms(String memberKey){
        // 마케팅 동의 타입
        List<MemberAgreementType> marketingTypes = Arrays.stream(MemberAgreementType.values())
                .filter(type -> !type.isRequired())
                .toList();

        List<MemberAgreementResponse> result = agreementRepository.getPersonalAgreements(memberKey, marketingTypes);
        if(result.isEmpty()){
            throw new CustomLogicException(ExceptionCode.NO_AGREED_TERMS);
        }

        return result;
    }
}
