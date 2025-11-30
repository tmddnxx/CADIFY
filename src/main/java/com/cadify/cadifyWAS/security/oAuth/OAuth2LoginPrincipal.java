package com.cadify.cadifyWAS.security.oAuth;

import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.security.common.CustomPrincipal;
import com.cadify.cadifyWAS.security.common.LoginType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class OAuth2LoginPrincipal implements OAuth2User, CustomPrincipal {

    @Getter
    private final LoginType loginType;
    @Getter
    private final String memberKey;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    public OAuth2LoginPrincipal(String memberKey,
                                Collection<? extends GrantedAuthority> authorities,
                                Map<String, Object> attributes){
        this.loginType = LoginType.OAUTH2;
        this.memberKey = memberKey;
        this. authorities = authorities;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return (String)attributes.get("sub");
    }
    // MemberRole 추출 메서드
    public MemberRole getRole() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.replace("ROLE_", ""))
                .map(MemberRole::valueOf)
                .findFirst()
                .orElseThrow();
    }
}
