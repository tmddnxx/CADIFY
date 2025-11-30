package com.cadify.cadifyWAS.security.common;

import com.cadify.cadifyWAS.model.entity.member.MemberRole;


public interface CustomPrincipal {
    String getMemberKey();
    LoginType getLoginType();
    MemberRole getRole();
}
