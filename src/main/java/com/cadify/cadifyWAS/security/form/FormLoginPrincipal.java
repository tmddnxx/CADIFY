package com.cadify.cadifyWAS.security.form;

import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.security.common.CustomPrincipal;
import com.cadify.cadifyWAS.security.common.LoginType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class FormLoginPrincipal implements UserDetails, CustomPrincipal {

    @Getter
    private final LoginType loginType;
    @Getter
    private final String memberKey;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String username;
    private final String password;

    public FormLoginPrincipal(String memberKey,
                              Collection<? extends GrantedAuthority> authorities,
                              String username, String password){
        this.loginType = LoginType.FORM;
        this.memberKey = memberKey;
        this.authorities = authorities;
        this.username = username;
        this.password = password;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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

    //  -----------------------------------> 미구현 선택 메서드

    // 계정 만료 여부 설정
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    // 계정 잠김 여부 설정
    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    // 비밀번호 만료 여부 설정
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    // 계정 활성화 여부 설정
    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
