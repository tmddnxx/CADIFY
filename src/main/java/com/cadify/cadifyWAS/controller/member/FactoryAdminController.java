package com.cadify.cadifyWAS.controller.member;

import com.cadify.cadifyWAS.model.dto.factory.FactoryAdminDTO;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.admin.AdminService;
import com.cadify.cadifyWAS.service.factory.FactoryAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/factory")
public class FactoryAdminController {
    private final FactoryAdminService factoryAdminService;
    private final AdminService adminService;

    @PostMapping("/join")
    public ResponseEntity<ResultResponse> registerFactoryAdmin(@Valid @RequestBody FactoryAdminDTO.JoinRequest request) {
        
        FactoryAdminDTO.InfoResponse response = adminService.registerManager(request);

        return ResponseEntity.ok().body(ResultResponse.of(ResultCode.SUCCESS));
    }
}
