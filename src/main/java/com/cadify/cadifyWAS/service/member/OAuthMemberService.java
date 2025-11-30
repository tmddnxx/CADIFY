package com.cadify.cadifyWAS.service.member;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.mapper.MemberMapper;
import com.cadify.cadifyWAS.model.dto.auth.AuthDTO;
import com.cadify.cadifyWAS.model.dto.member.MemberDTO;
import com.cadify.cadifyWAS.model.entity.factory.FactoryAdmin;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.model.entity.member.OAuthMember;
import com.cadify.cadifyWAS.repository.factory.admin.FactoryAdminRepository;
import com.cadify.cadifyWAS.repository.member.OAuthMemberRepository;
import com.cadify.cadifyWAS.repository.token.JwtRepository;
import com.cadify.cadifyWAS.security.common.LoginType;
import com.cadify.cadifyWAS.security.jwt.JwtPrincipal;
import com.cadify.cadifyWAS.security.jwt.JwtProvider;
import com.cadify.cadifyWAS.util.JwtUtil;
import com.cadify.cadifyWAS.util.api.MessageAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthMemberService {

    private final OAuthMemberRepository oAuthMemberRepository;
    private final FactoryAdminRepository factoryAdminRepository;
    private final MessageAPI messageAPI;
    private final JwtUtil jwtUtil;
    private final MemberMapper memberMapper;
    private final JwtProvider jwtProvider;
    private final JwtRepository jwtRepository;

    // 기본 사용자 정보 가져오기
    @Transactional
    public MemberDTO.MemberInfo getMemberInfo() {
        JwtPrincipal principal = jwtUtil.getAuthPrincipalObject();

        LoginType loginType = principal.getLoginType();
        String memberKey = principal.getMemberKey();
        OAuthMember member;
        FactoryAdmin admin;

        MemberDTO.MemberInfo memberInfo;
        // 로그인 타입 체크 & 타입에 맞는 테이블에서 객체 조회
        if(loginType.equals(LoginType.FORM)){
            admin = factoryAdminRepository.findByMemberKeyAndDeletedFalse(memberKey)
                    .orElseThrow(() -> new CustomLogicException(ExceptionCode.ADMIN_NOT_FOUND));
            memberInfo = new MemberDTO.MemberInfo(
                    admin.getName(), admin.getEmail(), admin.getPhone(), null, null, admin.getRole().name());
        }else if(loginType.equals(LoginType.OAUTH2)){
            member = oAuthMemberRepository.findByMemberKeyAndDeletedFalse(memberKey)
                    .orElseThrow(() -> new CustomLogicException(ExceptionCode.MEMBER_NOT_FOUND));
            memberInfo = new MemberDTO.MemberInfo(
                    member.getMemberName(), member.getEmail(), member.getPhone(), null, null, member.getRole().name());
        }else{
            throw new CustomLogicException(ExceptionCode.INVALID_LOGIN_TYPE);
        }

        return memberInfo;
    }

    // 최초 로그인 시 role 선택 후 기본 사용자 정보 리턴
    @Transactional
    public AuthDTO.AssignRoleResult assignMemberRole(MemberRole role) {
        // 최초 로그인 (회원가입) 이후의 초기 역할 선택에 해당하는지 검증.
        MemberRole currentRole = jwtUtil.getAuthPrincipalObject().getRole();
        if (!currentRole.equals(MemberRole.VISITOR)) {
            throw new CustomLogicException(ExceptionCode.INVALID_ASSIGN_ROLE);
        }

        // 조회 ( 인증객체 기반 )
        OAuthMember member = jwtUtil.getLoginMember();
        // MemberRole 업데이트
        member.assignRoleForFirstLogin(role);
        // 업데이트 반영
        oAuthMemberRepository.save(member);


        log.info("After Assign Role : " + member.getRole());

        // role 변경에 따른 새로운 토큰 전달
        String newAccessToken = jwtProvider.generateAccessToken(member.getMemberKey(), LoginType.OAUTH2, member.getRole());


        return new AuthDTO.AssignRoleResult(memberMapper.MemberToMemberInfo(member), newAccessToken);
    }

    // 핸드폰 인증 요청 ( 인증번호, 만료시간 리턴. )
    @Transactional
    public AuthDTO.AuthSMSResponse getSmsAuthCode(AuthDTO.PhoneAuthRequestDTO request) {
        String authCode = messageAPI.sendAuthCode(request.getPhone());
        String expiredAt = Instant.now().toString();

        return AuthDTO.AuthSMSResponse.builder()
                .authCode(authCode)
                .expiredAt(expiredAt)
                .build();
    }

    // 핸드폰 인증 검증 ( 핸드폰 + 인증번호 )사용해서 Redis 데이터 검증
    @Transactional
    public void verifySMSAuthCode(AuthDTO.VerifyAuthCodeRequestDTO request) {
        if (!messageAPI.verifyAuthCode(request.getPhone(), request.getAuthCode())) {
            throw new CustomLogicException(ExceptionCode.INVALID_AUTH_CODE);
        }
        try {
            OAuthMember member = jwtUtil.getLoginMember();
            member.insertPhone(request.getPhone());
            oAuthMemberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            throw new CustomLogicException(ExceptionCode.PHONE_ALREADY_EXISTS);
        }

    }

    // 사용자 정보 업데이트 후 업데이트 된 사용자 정보 리턴
    @Transactional
    public MemberDTO.MemberInfo updateMemberInfo(MemberDTO.UpdateMember request) {
        JwtPrincipal principal = jwtUtil.getAuthPrincipalObject();
        // 개인 회원인지 확인
        isPersonalMember(principal.getRole());
        // 업데이트 (회원 이름)
        OAuthMember member = jwtUtil.getLoginMember();
        member.update(request);
        oAuthMemberRepository.save(member);
        return memberMapper.MemberToMemberInfo(member);
    }

    // 회원 탈퇴 ( 소프트 딜리트 )
    @Transactional
    public void deleteMember(){
        JwtPrincipal principal = jwtUtil.getAuthPrincipalObject();

        OAuthMember member = oAuthMemberRepository.findByMemberKeyAndDeletedFalse(principal.getMemberKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.MEMBER_NOT_FOUND));



        member.softDelete();

        oAuthMemberRepository.save(member);
    }

// 내부 유틸 메서드
    // 역할이 USER 인지 확인
    private void isPersonalMember(MemberRole role) {
        if (!role.equals(MemberRole.USER)) {
            throw new CustomLogicException(ExceptionCode.NOT_USER_MEMBER);
        }
    }
}