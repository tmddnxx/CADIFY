package com.cadify.cadifyWAS.controller.company;

import com.cadify.cadifyWAS.model.dto.company.CompanyDTO;
import com.cadify.cadifyWAS.model.dto.company.CompanyManagerResponse;
import com.cadify.cadifyWAS.model.dto.company.CompanyResponse;
import com.cadify.cadifyWAS.service.member.CompanyManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyManagerService companyService;

    // 관리자 추가
    @PostMapping("/manager")
    public ResponseEntity<List<CompanyManagerResponse>>registerManager(@Valid @RequestBody CompanyDTO.RegisterManager request){
        List<CompanyManagerResponse> response = companyService.registerManager(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 관리자 리스트 조회 ( 회사 내 )
    @GetMapping("/manager/list")
    public ResponseEntity<List<CompanyManagerResponse>>getManagerList(){
        List<CompanyManagerResponse> response = companyService.getManagerList();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 관리자 정보 수정
    @PatchMapping("/manager")
    public ResponseEntity<CompanyManagerResponse>updateManager(@Valid @RequestBody CompanyDTO.UpdateManager request){
        CompanyManagerResponse response = companyService.updateManager(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 관리자 삭제
    @DeleteMapping("/manager")
    public ResponseEntity<List<CompanyManagerResponse>>deleteManager(@Valid @RequestBody CompanyDTO.DeleteManagerRequest request){
        List<CompanyManagerResponse> response = companyService.deleteManager(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 회사 정보 조회
    @GetMapping
    public ResponseEntity<CompanyResponse>getCompanyInfo(){
        CompanyResponse response = companyService.getCompanyInfo();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 회사 정보 수정
    @PatchMapping
    public ResponseEntity<CompanyResponse>updateCompanyInfo(@Valid @RequestBody CompanyDTO.UpdateCompanyInfoRequest request){
        CompanyResponse response = companyService.updateCompanyInfo(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
