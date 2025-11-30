package com.cadify.cadifyWAS.security.jwt;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.security.common.LoginType;
import com.cadify.cadifyWAS.security.form.CustomUserDetailsService;
import com.cadify.cadifyWAS.security.oAuth.CustomOAuth2UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomOAuth2UserService oAuth2UserService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/factory/login") || path.startsWith("/api/auth/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 유요한 Access Token 으로 접근했을 때.
        try{
            String token = jwtProvider.resolveToken(request);
            log.info("AccessToken in Header : {} ", token);
            if(token != null){
                // token 검증 후 claims 반환
                Claims claims = jwtProvider.parseClaims(token);
                String memberKey = claims.getSubject();
                log.info("memberKey : " + memberKey);
                LoginType loginType = LoginType.valueOf((String) claims.get("loginType"));
                log.info("loginType : " + loginType);
                MemberRole role = MemberRole.valueOf((String) claims.get("role"));
                log.info("memberRole : " + role);

                // Authentication 에 저장될 사용자 객체
                JwtPrincipal principal = new JwtPrincipal(claims.getSubject(), loginType, role);

                // 인증 객체 생성
                JwtAuthenticationToken authentication =
                        new JwtAuthenticationToken(principal, List.of(new SimpleGrantedAuthority("ROLE_" + principal.getRole().name())));
                // SecurityContext 에 인증 정보 주입
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (ExpiredJwtException e) {
            // 만료된 토큰일 경우
            log.info("Access Token 만료됨: {}", e.getMessage());
            throw new CustomLogicException(ExceptionCode.EXPIRED_ACCESS_TOKEN);

        } catch (JwtException e) {
            // 유효하지 않은 토큰일 경우, 검증 실패
            log.warn("Access Token 검증 실패: {}", e.getMessage());
            throw new CustomLogicException(ExceptionCode.INVALID_ACCESS_TOKEN);
        }

        filterChain.doFilter(request, response);
    }

    // 필요 없음 ?
//    // 로그인 타입 검증 ( From, OAuth2 )
//    private JwtPrincipal loadPrincipalByLoginType(LoginType loginType, String memberKey){
//        if (loginType == LoginType.OAUTH2) {
//            return oAuth2UserService.loadUserByMemberKey(memberKey);
//        } else {
//            return userDetailsService.loadUserByMemberKey(memberKey);
//        }
//    }
}