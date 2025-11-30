package com.cadify.cadifyWAS.security.form;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.entity.factory.FactoryAdmin;
import com.cadify.cadifyWAS.repository.factory.admin.FactoryAdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final FactoryAdminRepository factoryAdminRepository;


    // Form 로그인시 호출, 인증 객체 반환: Principal
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<FactoryAdmin> optionalAdmin = factoryAdminRepository.findByUsernameAndDeletedFalse(username);

        log.info("ID : " + username);

        FactoryAdmin admin = optionalAdmin.orElseThrow(
                () -> new CustomLogicException(ExceptionCode.MEMBER_NOT_FOUND)
        );

        log.info(admin.getUsername() + " : " + admin.getPassword());

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + admin.getRole().name());

        return new FormLoginPrincipal(admin.getMemberKey(), List.of(authority), admin.getUsername(), admin.getPassword());
    }

//    // JwtAuthenticationFilter 전용 인증 사용자 반환
//    public JwtPrincipal loadUserByMemberKey(String memberKey){
//        Optional<FactoryAdmin> optionalAdmin = factoryAdminRepository.findByMemberKey(memberKey);
//
//        FactoryAdmin admin = optionalAdmin.orElseThrow(
//                () -> new CustomLogicException(ExceptionCode.MEMBER_NOT_FOUND)
//        );
//
//        return new JwtPrincipal(admin.getMemberKey(), LoginType.FORM, admin.getRole());
//    }
}
