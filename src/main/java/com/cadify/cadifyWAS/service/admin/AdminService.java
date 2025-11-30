package com.cadify.cadifyWAS.service.admin;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.mapper.EstimateMapper;
import com.cadify.cadifyWAS.model.dto.admin.estimate.AdminEstimateDTO;
import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.cadify.cadifyWAS.model.entity.Files.QEstimate;
import com.cadify.cadifyWAS.model.entity.Files.QFiles;
import com.cadify.cadifyWAS.repository.OrderItemRepository;
import com.cadify.cadifyWAS.repository.OrderRepository;
import com.cadify.cadifyWAS.repository.admin.estimate.AdminEstimateQueryRepository;
import com.cadify.cadifyWAS.mapper.FactoryAdminMapper;
import com.cadify.cadifyWAS.model.dto.factory.FactoryAdminDTO;
import com.cadify.cadifyWAS.model.entity.factory.FactoryAdmin;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.repository.OrderItemRepository;
import com.cadify.cadifyWAS.repository.OrderRepository;
import com.cadify.cadifyWAS.repository.factory.FactoryRepository;
import com.cadify.cadifyWAS.repository.factory.admin.FactoryAdminRepository;
import com.cadify.cadifyWAS.repository.member.OAuthMemberRepository;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.security.jwt.JwtPrincipal;
import com.cadify.cadifyWAS.util.JwtUtil;
import com.cadify.cadifyWAS.util.PrivateValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springdoc.core.service.GenericResponseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Log4j2
@Service
public class AdminService {

    private final AdminEstimateQueryRepository adminEstimateQueryRepository;
    private final EstimateMapper estimateMapper;
    private final PrivateValue privateValue;
    private final FactoryRepository factoryRepository;
    private final FactoryAdminRepository factoryAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final FactoryAdminMapper factoryAdminMapper;
    private final JwtUtil jwtUtil;

    public List<AdminEstimateDTO.EstimateResponse> getAllEstimates() {
        List<Tuple> estimatesWithFiles = adminEstimateQueryRepository.getEstimatesWithFiles();
        if (estimatesWithFiles.isEmpty()) {
            return List.of();
        }

        return estimatesWithFiles.stream()
                .map(tuple -> {
                    Estimate estimate = tuple.get(QEstimate.estimate);
                    AdminEstimateDTO.EstimateResponse response = null;
                    try {
                        response = estimateMapper.estimateResponseToAdmin(estimate);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    response.setStepS3(tuple.get(QFiles.files.s3StepAddress)); // S3 URL 추출
                    response.setImageUrl(tuple.get(QFiles.files.imageAddress)); // S3 URL 추출
                    return response;
                }).toList();
    }

    public AdminEstimateDTO.EstimateResponse getEstimateByKey(String estKey) {
        Tuple tuple = adminEstimateQueryRepository.getEstimateWithFilesByKey(estKey);
        if (tuple == null) {
            throw new CustomLogicException(ExceptionCode.ESTIMATE_NOT_FOUND);
        }
        Estimate estimate = tuple.get(QEstimate.estimate);
        AdminEstimateDTO.EstimateResponse response = null;
        try {
            response = estimateMapper.estimateResponseToAdmin(estimate);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        response.setStepS3(tuple.get(QFiles.files.s3StepAddress)); // S3 URL 추출
        response.setImageUrl(tuple.get(QFiles.files.imageAddress)); // S3 URL 추출

        return response;
    }

    // 공장 관리자 등록 -> SuperAdminService ?
    @Transactional
    public FactoryAdminDTO.InfoResponse registerManager(FactoryAdminDTO.JoinRequest request) {
        // 관리자 검증
        isValidAdmin();

        // 존재하는 공장인지 확인
        factoryRepository.findFactoryByFactoryKeyAndDeletedFalse(request.getFactoryKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.FACTORY_NOT_FOUND));

        // 이미 존재하는 관리자 or 중복된 아이디인지 확인
        Optional<FactoryAdmin> optionalAdmin = factoryAdminRepository
                .findFirstByEmailOrUsernameAndDeletedFalse(request.getEmail(), request.getUsername());

        if(optionalAdmin.isPresent()){
            FactoryAdmin admin = optionalAdmin.get();

            if(admin.getEmail().equals(request.getEmail())){
                // 이미 존재하는 사용자 -> 이메일 중복
                throw new CustomLogicException(ExceptionCode.ALREADY_EXIST_EMAIL);
            }else if(admin.getUsername().equals(request.getUsername())){
                // 이미 사용중인 아이디
                throw new CustomLogicException(ExceptionCode.ALREADY_EXIST_USERNAME);
            }
        }

        // 사용자 객체 생성
        FactoryAdmin newAdmin = FactoryAdmin.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .role(MemberRole.FACTORY)
                .factoryKey(request.getFactoryKey())
                .build();

        factoryAdminRepository.save(newAdmin);

        return factoryAdminMapper.factoryAdminToInfoResponse(newAdmin);
    }


// 내부 메서드
    // 관리자 검증
    private void isValidAdmin(){
        JwtPrincipal jwtPrincipal = jwtUtil.getAuthPrincipalObject();
        if( jwtPrincipal.getRole() != MemberRole.ADMIN ){
            throw new CustomLogicException(ExceptionCode.NOT_SUPER_ADMIN);
        }
    }
}
