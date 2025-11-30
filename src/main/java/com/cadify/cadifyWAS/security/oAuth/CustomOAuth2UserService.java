package com.cadify.cadifyWAS.security.oAuth;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.repository.member.OAuthMemberRepository;
import com.cadify.cadifyWAS.model.entity.member.OAuthMember;
import com.cadify.cadifyWAS.util.UUIDGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final OAuthMemberRepository oAuthMemberRepository;

    // OAuth2.0 로그인 과정에서 호출. SecurityContext 인증 토큰에 OAuth2LoginPrincipal 저장
    @Override
    public OAuth2LoginPrincipal loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuthMember member = processOAuthUser(registrationId, attributes);

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + member.getRole().name());

        // 인증 관련 커스텀 객체 리턴
        return new OAuth2LoginPrincipal(member.getMemberKey(), List.of(authority), attributes);
    }

//    // 토큰에서 memberKey로 OAuth2 사용자 조회 ( JwtAuthenticationFilter 용 )
//    @Transactional
//    public JwtPrincipal loadUserByMemberKey(String memberKey) {
//        OAuthMember member = oAuthMemberRepository.findByMemberKey(memberKey)
//                .orElseThrow(() -> new UsernameNotFoundException("OAuth member not found: " + memberKey));
//        log.info("member name" + member.getMemberName());
//        return new JwtPrincipal(member.getMemberKey(), LoginType.OAUTH2, member.getRole());
//    }

    // authKey 기반, 회원 조회 후 로그인 or 회원가입 메서드
    @Transactional
    private OAuthMember processOAuthUser(String provider, Map<String, Object> attributes){
        String authKey = extractProviderId(provider, attributes);
        Optional<OAuthMember> optionalMember = oAuthMemberRepository.findByProviderAndAuthKey(provider, authKey);

        if(optionalMember.isEmpty()){
            OAuthMember newMember = OAuthMember.builder()
                    .memberKey(UUIDGenerator.generateUUID().toString())
                    .email((String)attributes.get("email"))
                    .memberName((String)attributes.get("name"))
                    .provider(provider)
                    .authKey(authKey)
                    .role(MemberRole.VISITOR)
                    .build();
            return oAuthMemberRepository.save(newMember);
        }else{
            return optionalMember.get();
        }
    }

    // OAuth provider 구분 및 검증
    private String extractProviderId(String provider, Map<String, Object> attributes){
        switch (provider.toLowerCase()){
            case "google": return (String)attributes.get("sub");
            default: throw new CustomLogicException(ExceptionCode.UNKNOWN_AUTH_PROVIDER);
        }
    }
}