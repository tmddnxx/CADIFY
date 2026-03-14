package com.cadify.cadifyWAS.controller.admin;

import com.cadify.cadifyWAS.model.dto.admin.estimate.AdminEstimateDTO;
import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.service.admin.AdminService;
import com.cadify.cadifyWAS.service.file.EstimateService;
import com.cadify.cadifyWAS.service.file.FilesByFactoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final EstimateService estimateService;
    private final FilesByFactoryService filesByFactoryService;

    @GetMapping("/estimates")
    public ResponseEntity<List<AdminEstimateDTO.EstimateResponse>> getAllEstimates() {
        return ResponseEntity.ok(adminService.getAllEstimates());
    }

    @GetMapping("/estimate/{estKey}")
    public ResponseEntity<AdminEstimateDTO.EstimateResponse> getEstimateByKey(@PathVariable("estKey") String estKey) {
        return ResponseEntity.ok(adminService.getEstimateByKey(estKey));
    }

    // stp 다운로드 url 반환
    @PostMapping(value = "/estimate/stp")
    public ResponseEntity<String> downloadSTP(@RequestBody EstimateDTO.Request request){
        String redirectUrl = estimateService.downloadSTP(request.getStpUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, redirectUrl);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    // 견적 관련 저장된 파일 주소
    @GetMapping("/files/{estKey}")
    public ResponseEntity<List<String>> downloadStepFile(@PathVariable("estKey") String estKey) {
        return new ResponseEntity<>(filesByFactoryService.getEstimateFileUrls(estKey), HttpStatus.OK);
    }
}
