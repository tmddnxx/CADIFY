package com.cadify.cadifyWAS.service.factory;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.mapper.FactoryAdminMapper;
import com.cadify.cadifyWAS.model.dto.factory.FactoryAdminDTO;
import com.cadify.cadifyWAS.model.entity.factory.FactoryAdmin;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.repository.factory.FactoryRepository;
import com.cadify.cadifyWAS.repository.factory.admin.FactoryAdminRepository;
import com.cadify.cadifyWAS.security.jwt.JwtPrincipal;
import com.cadify.cadifyWAS.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FactoryAdminService {

    private final FactoryAdminRepository factoryAdminRepository;
    private final FactoryRepository factoryRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final FactoryAdminMapper factoryAdminMapper;

    // 공장 관리자 수정


    // 공장 관리자 삭제
    @Transactional
    public void deleteFactoryAdmin() {
        FactoryAdmin factoryAdmin = isValidFactoryAdmin();
        factoryAdmin.softDelete();
        factoryAdminRepository.save(factoryAdmin);
    }


// 내부 유틸 메서드
    // 유효한 공장 관리자 조회
    private FactoryAdmin isValidFactoryAdmin() {
        JwtPrincipal principal = jwtUtil.getAuthPrincipalObject();

        if ((principal.getRole() != MemberRole.FACTORY) || principal.getRole() != MemberRole.ADMIN) {
            throw new CustomLogicException(ExceptionCode.NOT_FACTORY_ADMIN);
        }

        return factoryAdminRepository.findByMemberKeyAndDeletedFalse(principal.getMemberKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ADMIN_NOT_FOUND));
    }
}
