package com.cadify.cadifyWAS.controller.files;

import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.file.EstimateService;
import com.cadify.cadifyWAS.service.orchestrator.EstimateCartFacade;
import com.cadify.cadifyWAS.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/estimate")
@RequiredArgsConstructor
public class EstimateController {

    private final EstimateService estimateService;
    private final JwtUtil jwtUtil;
    private final EstimateCartFacade estimateCartFacade;

    // 견적목록 조회
    @GetMapping({"/list", "/list/{folderKey}"})
    public ResponseEntity<EstimateDTO.ListResponse> getEstimateList(@PathVariable(value = "folderKey", required = false) String folderKey) {
        log.info("폴더 키 : {}", folderKey);
        String memberKey = jwtUtil.getAuthPrincipal();
        return new ResponseEntity<>(estimateService.estimateList(memberKey, folderKey), HttpStatus.OK);
    }

    // 단일 견적 조회 + json
    @GetMapping(value = "/{estKey}")
    public ResponseEntity<EstimateDTO.Response> getEstimate(@PathVariable("estKey") String estKey) {
        EstimateDTO.Response response = estimateService.getEstimate(estKey);
        return ResponseEntity.ok(response);
    }

    // 모델링 이름 업데이트
    @PatchMapping(value = "/fileName/{estKey}/{fileName}")
    public ResponseEntity<EstimateDTO.StatusResponse> patchFileName(@PathVariable("estKey") String estKey, @PathVariable("fileName") String fileName) {
        EstimateDTO.StatusResponse statusResponse = estimateService.patchFileName(estKey, fileName);
        return ResponseEntity.ok(statusResponse);
    }

    // 판금 옵션 업데이트
    @PutMapping(value = "/metal/option")
    public ResponseEntity<EstimateDTO.StatusResponse> putOption(@Valid @RequestBody EstimateDTO.MetalOptionPut optionPut) {
        EstimateDTO.StatusResponse statusResponse = estimateCartFacade.deleteCartItemAtEstimateModifyingWithMetal(optionPut);
        return ResponseEntity.ok(statusResponse);
    }

    // 절삭 옵션 업데이트
    @PutMapping(value = "/cnc/option")
    public ResponseEntity<EstimateDTO.StatusResponse> putCncOption(@Valid @RequestBody EstimateDTO.CnCOptionPut cncOptionPut) {
        EstimateDTO.StatusResponse statusResponse = estimateCartFacade.deleteCartItemAtEstimateModifyingWithCNC(cncOptionPut);
        return ResponseEntity.ok(statusResponse);
    }

    // 뷰어에서 모델링 dxf 파일 업로드
    @PatchMapping(value = "/dxf/{estKey}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EstimateDTO.StatusResponse> patchDxf(@PathVariable("estKey") String estKey, @RequestPart("file") MultipartFile file) {
        String memberKey = jwtUtil.getAuthPrincipal();
        EstimateDTO.StatusResponse statusResponse = estimateService.patchDxf(memberKey, estKey, file);
        return ResponseEntity.ok(statusResponse);
    }

    // 뷰어에서 dxf 파일 삭제
    @DeleteMapping(value = "/dxf/{estKey}")
    public ResponseEntity<ResultResponse> deleteDxf(@PathVariable("estKey") String estKey) {
        ResultResponse resultResponse = estimateService.deleteDxf(estKey);
        return ResponseEntity.ok().body(resultResponse);
    }

    // stp 다운로드 url 반환
    @PostMapping(value = "/stp")
    public ResponseEntity<String> downloadSTP(@RequestBody EstimateDTO.Request request) {
        String redirectUrl = estimateService.downloadSTP(request.getStpUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, redirectUrl);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    // 메모 저장
    @PostMapping("/memo")
    public ResponseEntity<ResultResponse> saveMemo(@Valid @RequestBody EstimateDTO.MemoPut memoPut) {
        estimateService.saveMemo(memoPut);
        return ResponseEntity.ok().body(ResultResponse.of(ResultCode.SUCCESS));
    }

    // 견적의 폴더이동
    @PostMapping("/moveFolder")
    public ResponseEntity<ResultResponse> moveEstimateFolder(@RequestBody EstimateDTO.MoveFolder move) {
        ResultResponse response = estimateService.moveEstimateFolder(move);
        return ResponseEntity.ok().body(response);
    }

    // 모델링 파일 삭제
    @PostMapping(value = "/delete")
    public ResponseEntity<ResultResponse> deleteFile(@RequestBody EstimateDTO.Delete delete) {
        ResultResponse response = estimateService.deleteEstimate(delete);
        return ResponseEntity.ok().body(response);
    }

    // 견적 요청 업데이트
    @PatchMapping(value = "/request/{estKey}/{id}")
    public ResponseEntity<ResultResponse> updateEstimateRequest(@PathVariable("estKey") String estKey,
            @PathVariable("id") String id) {
        ResultResponse response = estimateService.updateEstimateRequestFlag(estKey, id);
        return ResponseEntity.ok().body(response);
    }

}
