package com.cadify.cadifyWAS.repository.member;

import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.model.entity.member.OAuthMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface  OAuthMemberRepository extends JpaRepository<OAuthMember, String> {

    Optional<OAuthMember> findByProviderAndAuthKey(String provider, String providerId);

    MemberRole findRoleByMemberKey(String memberKey);

    Optional<OAuthMember> findByEmailAndDeletedFalse(String email);

    Optional<OAuthMember> findById(String memberKey);

    // deleted 칼럼 false 사용자만 조회 (소프트 딜리트된 사용자 필터링)
    Optional<OAuthMember> findByMemberKeyAndDeletedFalse(String memberKey);
}

