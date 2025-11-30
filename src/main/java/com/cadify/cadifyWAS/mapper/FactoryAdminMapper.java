package com.cadify.cadifyWAS.mapper;

import com.cadify.cadifyWAS.model.dto.factory.FactoryAdminDTO;
import com.cadify.cadifyWAS.model.entity.factory.FactoryAdmin;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FactoryAdminMapper {

    private final PasswordEncoder passwordEncoder;

    public FactoryAdmin joinRequestToFactoryAdmin(FactoryAdminDTO.JoinRequest dto){
        return FactoryAdmin.builder()
                .memberKey(UUIDGenerator.generateUUID().toString())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .role(MemberRole.FACTORY)
                .build();
    }

    public FactoryAdminDTO.InfoResponse factoryAdminToInfoResponse(FactoryAdmin admin){
        return FactoryAdminDTO.InfoResponse.builder()
                .username(admin.getUsername())
                .email(admin.getEmail())
                .phone(admin.getPhone())
                .build();
    }
}
