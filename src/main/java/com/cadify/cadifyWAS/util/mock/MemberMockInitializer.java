package com.cadify.cadifyWAS.util.mock;

import com.cadify.cadifyWAS.model.entity.factory.FactoryAdmin;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.model.entity.member.OAuthMember;
import com.cadify.cadifyWAS.repository.factory.admin.FactoryAdminRepository;
import com.cadify.cadifyWAS.repository.member.OAuthMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberMockInitializer {

    private final OAuthMemberRepository memberRepository;
    private final FactoryAdminRepository factoryAdminRepository;

    public void init() {
        // testAdmin ( 서비스 관리자 ) * 임시 role = USER
        saveMember(findMemberKey("testAdmin"), "테스트_관리자", "testAdmin01@test.com", "010-1111-1111",
                MemberRole.USER, "0001");
        // testUser ( 유저 테스트 )
        saveMember(findMemberKey("testUser"), "테스트_유저", "testUser01@test.com", "010-5555-5555",
                MemberRole.USER, "0002");
    }

    private void saveMember(String key, String name, String email, String phone, MemberRole role, String authKey) {
        if (memberRepository.findByEmailAndDeletedFalse(email).isEmpty()) {
            memberRepository.save(OAuthMember.builder()
                    .memberKey(key)
                    .memberName(name)
                    .email(email)
                    .phone(phone)
                    .addressNumber(null)
                    .addressDetail(null)
                    .role(role)
                    .provider("google")
                    .authKey(authKey)
                    .build()
            );
        }
    }

    private String findMemberKey(String username){
        FactoryAdmin admin = factoryAdminRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow();
        return admin.getMemberKey();
    }
}
