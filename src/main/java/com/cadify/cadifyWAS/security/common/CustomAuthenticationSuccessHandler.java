package com.cadify.cadifyWAS.security.common;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.security.form.FormLoginPrincipal;
import com.cadify.cadifyWAS.security.jwt.JwtProvider;
import com.cadify.cadifyWAS.security.oAuth.OAuth2LoginPrincipal;
import com.cadify.cadifyWAS.service.auth.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    // 로그인 성공시 호출.
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 로그인 인증객체 추출
        Object principal = authentication.getPrincipal();

        // refresh token 생성 정보
        String memberKey;
        LoginType loginType;
        MemberRole memberRole;

        // 로그인 인증 토큰 principal 매칭
        if(principal instanceof FormLoginPrincipal formPrincipal){
            memberKey = formPrincipal.getMemberKey();
            log.info("로그인 Principal 멤버 키 : " + memberKey);
            loginType = formPrincipal.getLoginType();
            log.info("로그인 Principal 로그인 타입 : " + loginType.name());
            memberRole = formPrincipal.getRole();
            log.info("로그인 Principal 역할 타입 : " + memberRole.name());

        } else if (principal instanceof OAuth2LoginPrincipal oAuthPrincipal){
            memberKey = oAuthPrincipal.getMemberKey();
            log.info("로그인 Principal 멤버 키 : " + memberKey);
            loginType = oAuthPrincipal.getLoginType();
            log.info("로그인 Principal 로그인 타입 : " + loginType.name());
            memberRole = oAuthPrincipal.getRole();
            log.info("로그인 Principal 역할 타입 : " + memberRole.name());
        } else {
            throw new CustomLogicException(ExceptionCode.UNKNOWN_LOGIN_PRINCIPAL);
        }

        String refreshToken = authService.generateRefreshTokenForLogin(memberKey, loginType, memberRole);

        // refresh token 전달용 http only cookie 설정
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true) // http only -> JS 접근 불가
                .secure(true)   // Https 에서만 동작
                .path("/")      // 전체 도메인 경로를 대상으로 쿠키 전송
                .maxAge(jwtProvider.getExpirationTimeRefreshToken())    // 쿠키 만료기간 설정: refresh token 유효기간과 동일
                .sameSite("None")    // 외부 사이트에서 쿠키 전송 제한, Lax : 안전한 네비게이션(GET, HEAD)만 허용
                .build();

        // 응답에 쿠키 적용
        response.addHeader("Set-Cookie", cookie.toString());

        // TEST: http only cookie 발급 확인
        log.info("로그인 성공 후, Http Only Refresh Token 발급 : {}", cookie.toString());

        // Redirect → 로그인 성공 처리 전용 프론트 페이지
        response.sendRedirect("https://cadify.kr/login/google");
    }
}
