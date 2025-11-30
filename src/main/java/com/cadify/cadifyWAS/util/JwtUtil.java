package com.cadify.cadifyWAS.util;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.entity.member.OAuthMember;
import com.cadify.cadifyWAS.repository.member.OAuthMemberRepository;
import com.cadify.cadifyWAS.security.jwt.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    private final OAuthMemberRepository oAuthMemberRepository;

    public OAuthMember getLoginMember() {
        // Authentication 객체에서 Principal을 가져옵니다.
        String memberKey = getAuthPrincipal();

        // memberKey를 사용하여 DB에서 OAuthMember 조회
        return oAuthMemberRepository.findByMemberKeyAndDeletedFalse(memberKey)
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    public String getAuthPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof JwtPrincipal jwtPrincipal) {
            return jwtPrincipal.getMemberKey();
        }

        throw new CustomLogicException(ExceptionCode.MEMBER_NOT_FOUND);
    }

    public JwtPrincipal getAuthPrincipalObject(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof JwtPrincipal jwtPrincipal) {
            return jwtPrincipal;
        }

        throw new CustomLogicException(ExceptionCode.MEMBER_NOT_FOUND);
    }
}