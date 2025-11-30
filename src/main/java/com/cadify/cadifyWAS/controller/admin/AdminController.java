package com.cadify.cadifyWAS.controller.admin;

import com.cadify.cadifyWAS.model.dto.admin.estimate.AdminEstimateDTO;
import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.service.admin.AdminService;
import com.cadify.cadifyWAS.service.file.EstimateService;
import com.cadify.cadifyWAS.service.file.FilesByFactoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Log4j2
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
        String stepFileUrl = filesByFactoryService.downloadStepFile(estKey);
        String dxfFileUrl = filesByFactoryService.downloadDxfByUser(estKey);
        String dxfFactoryFileUrl = null;
        try {
            dxfFactoryFileUrl = filesByFactoryService.downloadDxfByFactory(estKey);
        }catch (Exception e) {
            System.out.println(e.getMessage());
            dxfFactoryFileUrl = null;
        }
        List<String> fileUrls = new ArrayList<>();
        if (stepFileUrl != null) fileUrls.add(stepFileUrl);
        if (dxfFileUrl != null) fileUrls.add(dxfFileUrl);
        if (dxfFactoryFileUrl != null) fileUrls.add(dxfFactoryFileUrl);
        return new ResponseEntity<>(fileUrls, HttpStatus.OK);
    }
}
