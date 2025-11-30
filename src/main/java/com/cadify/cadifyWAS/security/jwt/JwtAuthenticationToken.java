package com.cadify.cadifyWAS.security.jwt;

import com.cadify.cadifyWAS.security.common.CustomPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final JwtPrincipal jwtPrincipal;
    private final String credentials;

    public JwtAuthenticationToken(JwtPrincipal jwtPrincipal, Collection<? extends GrantedAuthority> authorities){
        super(authorities);
        this.jwtPrincipal = jwtPrincipal;
        this.credentials = null; // JWT 자체가 자격 증명에 해당, 별도의 자격 증명 정보 필요 없음.
        setAuthenticated(true); // JWT 인증은 인증된 상태로 설정
    }

    @Override
    public Object getPrincipal() {
        return jwtPrincipal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }
}
