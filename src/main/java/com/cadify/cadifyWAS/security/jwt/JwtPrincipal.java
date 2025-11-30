package com.cadify.cadifyWAS.security.jwt;

import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.security.common.CustomPrincipal;
import com.cadify.cadifyWAS.security.common.LoginType;

public class JwtPrincipal implements CustomPrincipal {

    private final String memberKey;
    private final LoginType loginType;
    private final MemberRole role;

    public JwtPrincipal(String memberKey, LoginType loginType, MemberRole role){
        this.memberKey = memberKey;
        this.loginType = loginType;
        this.role = role;
    }

    @Override
    public String getMemberKey() {
        return memberKey;
    }

    @Override
    public LoginType getLoginType() {
        return loginType;
    }

    @Override
    public MemberRole getRole() {
        return role;
    }
}
