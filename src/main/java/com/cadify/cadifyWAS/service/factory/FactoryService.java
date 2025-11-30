package com.cadify.cadifyWAS.service.factory;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.dto.factory.FactoryDTO;
import com.cadify.cadifyWAS.model.entity.factory.Factory;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.repository.factory.FactoryQueryRepository;
import com.cadify.cadifyWAS.repository.factory.FactoryRepository;
import com.cadify.cadifyWAS.security.jwt.JwtPrincipal;
import com.cadify.cadifyWAS.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FactoryService {

    private final JwtUtil jwtUtil;
    private final FactoryRepository factoryRepository;
    private final FactoryQueryRepository factoryQueryRepository;

    // 공장 등록
    @Transactional
    public FactoryDTO.FactoryInfoResponse getFactoryInfo(){
        Factory factory = isValidFactory();
        return null;
    }
    // 공장 수정
    @Transactional
    public FactoryDTO.FactoryInfoResponse updateFactoryInfo(){
        Factory factory = isValidFactory();
        return null;
    }
    // 공장 삭제 (소프트 딜리트)
    @Transactional
    public void deleteFactory(String factoryKey){
        JwtPrincipal principal = jwtUtil.getAuthPrincipalObject();
        if(principal.getRole() != MemberRole.ADMIN){
            throw new CustomLogicException(ExceptionCode.NOT_SUPER_ADMIN);
        }
        Factory factory = factoryRepository.findFactoryByFactoryKeyAndDeletedFalse(factoryKey)
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.FACTORY_NOT_FOUND));

        factory.softDelete();

        factoryRepository.save(factory);
    }





// 내부 유틸 메서드
    // 유효한 공장 조회
    private Factory isValidFactory(){
        JwtPrincipal principal = jwtUtil.getAuthPrincipalObject();
        // 역할 검증
        if( (principal.getRole() != MemberRole.ADMIN) || (principal.getRole() != MemberRole.FACTORY)){
            throw new CustomLogicException(ExceptionCode.NOT_FACTORY_ADMIN);
        }

        return factoryQueryRepository.getFactoryFromPrincipal(principal.getMemberKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.FACTORY_NOT_FOUND));
    }
}
