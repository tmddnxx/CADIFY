package com.cadify.cadifyWAS.service.member;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.dto.auth.AuthDTO;
import com.cadify.cadifyWAS.model.dto.company.CompanyDTO;
import com.cadify.cadifyWAS.model.dto.company.CompanyManagerResponse;
import com.cadify.cadifyWAS.model.dto.company.CompanyResponse;
import com.cadify.cadifyWAS.model.entity.member.CompanyManager;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;

import com.cadify.cadifyWAS.model.entity.member.OAuthMember;
import com.cadify.cadifyWAS.repository.company.CompanyManagerRepository;
import com.cadify.cadifyWAS.repository.member.OAuthMemberRepository;
import com.cadify.cadifyWAS.security.jwt.JwtPrincipal;
import com.cadify.cadifyWAS.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyManagerService {

    private final OAuthMemberService oAuthMemberService;
    private final JwtUtil jwtUtil;
    private final CompanyManagerRepository companyManagerRepository;
    private final OAuthMemberRepository companyRepository;

    // 기업 회원 전환 후 관리자 추가
    @Transactional
    public List<CompanyManagerResponse> registerManager(CompanyDTO.RegisterManager request) {
        JwtPrincipal principal = isValidCompanyManager();
        // 핸드폰 인증
        oAuthMemberService.verifySMSAuthCode(
                new AuthDTO.VerifyAuthCodeRequestDTO(request.getPhone(), request.getAuthCode())
        );
        // 관리자 추가
        companyManagerRepository.save(
                CompanyManager.builder()
                        .memberKey(principal.getMemberKey())
                        .department(request.getDepartment())
                        .position(request.getPosition())
                        .managerName(request.getManagerName())
                        .phone(request.getPhone())
                        .build()
        );

        return companyManagerRepository.findCompanyManagerList(principal.getMemberKey());
    }
    // 회사 내 등록된 관리자 리스트 조회
    @Transactional(readOnly = true)
    public List<CompanyManagerResponse> getManagerList(){
        JwtPrincipal principal = isValidCompanyManager();
        return companyManagerRepository.findCompanyManagerList(principal.getMemberKey());
    }

    // 관리자 업데이트
    @Transactional
    public CompanyManagerResponse updateManager(CompanyDTO.UpdateManager request){
        // MemberRole 검증
        JwtPrincipal principal = isValidCompanyManager();
        // managerKey 기반 조회 ( pk 기반 조회 성능 최적화 )
        CompanyManager manager = companyManagerRepository.findByManagerKey(request.getManagerKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.MANAGER_NOT_FOUND));
        // 소유권 확인
        verifyManagerOwnership(manager, principal.getMemberKey());

        // 매니저 정보 업데이트
        manager.updateManagerInfo(request.getDepartment(), request.getPosition(), request.getManagerName());

        // 변경된 정보 저장
        companyManagerRepository.save(manager);

        return new CompanyManagerResponse(
                manager.getManagerKey(), manager.getPhone(), manager.getDepartment(), manager.getPosition(), manager.getManagerName());
    }

    // 관리자 삭제 ( 수정 중 )
    @Transactional
    public List<CompanyManagerResponse> deleteManager(String managerKey){
        JwtPrincipal principal = isValidCompanyManager();
        CompanyManager manager = companyManagerRepository.findByManagerKey(managerKey)
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.MANAGER_NOT_FOUND));
        // 소유권 확인
        verifyManagerOwnership(manager, principal.getMemberKey());

        // 삭제
        manager.softDelete();

        companyManagerRepository.save(manager);

        return null;
    }

    // 회사 정보 조회
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyInfo(){
        OAuthMember company = jwtUtil.getLoginMember();
        if(!company.getRole().equals(MemberRole.COMPANY)){
            throw new CustomLogicException(ExceptionCode.NOT_COMPANY_MEMBER);
        }
        return new CompanyResponse(company.getMemberName(), company.getEmail());
    }

    // 회사 정보 수정
    @Transactional
    public CompanyResponse updateCompanyInfo(String companyName){
        OAuthMember company = jwtUtil.getLoginMember();
        if(!company.getRole().equals(MemberRole.COMPANY)){
            throw new CustomLogicException(ExceptionCode.NOT_COMPANY_MEMBER);
        }
        // 업데이트 ( 회사명 )
        company.updateForCompany(companyName);
        companyRepository.save(company);

        return new CompanyResponse(company.getMemberName(), company.getEmail());
    }


// --------------------------   내부 Util

    // 관리자의 수정 권한이 있는지 확인. ( 관리자가 해당 사용자(로그인된 유저)의 소유인지 확인 )
    private void verifyManagerOwnership(CompanyManager manager, String memberKey){
        if( ! manager.getMemberKey().equals(memberKey))
        {
            throw new CustomLogicException(ExceptionCode.INVALID_UPDATE_MANAGER);
        }
    }

    // MemberRole 이 Company 인지 검증 후 토큰 인증 객체 리턴
    private JwtPrincipal isValidCompanyManager(){
        JwtPrincipal principal = jwtUtil.getAuthPrincipalObject();

        // member role 확인, COMPANY 여야 함
        if(principal.getRole() != MemberRole.COMPANY){
            throw new CustomLogicException(ExceptionCode.NOT_COMPANY_MEMBER);
        }

        return principal;
    }
}