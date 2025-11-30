package com.cadify.cadifyWAS.controller.member;

import com.cadify.cadifyWAS.model.dto.factory.FactoryAdminDTO;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.admin.AdminService;
import com.cadify.cadifyWAS.service.factory.FactoryAdminService;
import com.cadify.cadifyWAS.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/factory")
public class FactoryAdminController {
    private final FactoryAdminService factoryAdminService;
    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    @PostMapping("/join")
    public ResponseEntity<ResultResponse> registerFactoryAdmin(@RequestBody FactoryAdminDTO.JoinRequest request) {
        
        FactoryAdminDTO.InfoResponse response = adminService.registerManager(request);

        return ResponseEntity.ok().body(ResultResponse.of(ResultCode.SUCCESS));
    }
}
