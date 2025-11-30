package com.cadify.cadifyWAS.mapper;

import com.cadify.cadifyWAS.model.dto.member.MemberDTO;
import com.cadify.cadifyWAS.model.entity.member.OAuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberMapper {

    public MemberDTO.MemberInfo MemberToMemberInfo(OAuthMember member){
        return MemberDTO.MemberInfo.builder()
                .memberName(member.getMemberName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .role(member.getRole().name())
                .build();
    }
}