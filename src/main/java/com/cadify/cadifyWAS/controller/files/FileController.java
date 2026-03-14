package com.cadify.cadifyWAS.controller.files;

import com.cadify.cadifyWAS.config.SseEmitters;
import com.cadify.cadifyWAS.model.dto.files.FileTask;
import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.file.FilesTaskService;
import com.cadify.cadifyWAS.service.file.enumValues.common.CommonDiff;
import com.cadify.cadifyWAS.service.file.enumValues.common.Material;
import com.cadify.cadifyWAS.service.file.enumValues.common.Roughness;
import com.cadify.cadifyWAS.service.file.enumValues.common.Surface;
import com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.material.MetalMaterialByThickness;
import com.cadify.cadifyWAS.service.file.rabbitMQ.FileTaskProducer;
import com.cadify.cadifyWAS.util.JwtUtil;
import com.cadify.cadifyWAS.service.file.FilesService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file")
@Slf4j
public class  FileController {

    private final FilesService filesService;
    private final JwtUtil jwtUtil;
    private final SseEmitters sseEmitters;
    private final FilesTaskService filesTaskService;
    private final FileTaskProducer fileTaskProducer;
    private final long MAXSIZE = 2 * 1024 * 1024; // 파일 크기 제한을 2MB로 설정
    private final long TOTALMAXSIZE = 40 * 1024 * 1024; // 파일 크기 제한을 40MB로 설정

    // 판금 업로드 처리 메소드
    @PostMapping(value = "/upload/metal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultResponse> uploadFile(@RequestParam("files") List<MultipartFile> files,
                                                     @RequestParam("folderKey") String folderKey) {
        return ResponseEntity.status(500).body(new ResultResponse(ResultCode.FAILED, "현재 서비스 점검중입니다."));
//        ResultResponse resultResponse;
//
//        try {
//            // 파일이 하나도 없다면 오류 반환
//            if (files.isEmpty()) {
//                resultResponse = new ResultResponse(ResultCode.FAILED, "업로드할 파일이 없습니다.");
//                return ResponseEntity.badRequest().body(resultResponse);
//            }
//
//            // 파일 크기 체크
//            int totalFileSize = 0;
//            for (MultipartFile file : files) {
//                totalFileSize += file.getSize();
//                if (file.getSize() > MAXSIZE) {
//                    resultResponse = new ResultResponse(ResultCode.FAILED, "파일 크기가 너무 큽니다. 각 파일당 최대 크기는 2MB입니다.");
//                    return ResponseEntity.badRequest().body(resultResponse);
//                }
//            }
//
//            if (totalFileSize > TOTALMAXSIZE) {
//                resultResponse = new ResultResponse(ResultCode.FAILED, "전체 파일 크기가 너무 큽니다. 최대 크기는 40MB입니다.");
//                return ResponseEntity.badRequest().body(resultResponse);
//            }
//
//            // 파일 업로드 처리
//            resultResponse = filesService.uploadFiles(files, folderKey, Method.METAL);
//
//        } catch (Exception e) {
//            // 파일 업로드 중 다른 예외 발생 시 처리
//            log.error("파일 업로드 중 오류 발생", e);
//            resultResponse = new ResultResponse(ResultCode.FAILED, e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultResponse);
//        }
//
//        return ResponseEntity.ok(resultResponse);
    }

    // 절삭 업로드 
    @PostMapping(value = "/upload/cnc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultResponse> uploadCncFile(@RequestParam("files") List<MultipartFile> files,
                                                                       @RequestParam("folderKey") String folderKey) {
        return ResponseEntity.status(500).body(new ResultResponse(ResultCode.FAILED, "현재 서비스 점검중입니다."));
//        ResultResponse resultResponse;
//
//        try {
//            // 파일이 하나도 없다면 오류 반환
//            if (files.isEmpty()) {
//                resultResponse = new ResultResponse(ResultCode.FAILED, "업로드할 파일이 없습니다.");
//                return ResponseEntity.badRequest().body(resultResponse);
//            }
//
//            // 파일 크기 체크
//            int totalFileSize = 0;
//            for (MultipartFile file : files) {
//                totalFileSize += file.getSize();
//                if (file.getSize() > MAXSIZE) {
//                    resultResponse = new ResultResponse(ResultCode.FAILED, "파일 크기가 너무 큽니다. 최대 크기는 2MB입니다.");
//                    return ResponseEntity.badRequest().body(resultResponse);
//                }
//            }
//
//            if (totalFileSize > TOTALMAXSIZE) {
//                resultResponse = new ResultResponse(ResultCode.FAILED, "전체 파일 크기가 너무 큽니다. 최대 크기는 40MB입니다.");
//                return ResponseEntity.badRequest().body(resultResponse);
//            }
//
//            // 파일 업로드 처리
//            resultResponse = filesService.uploadFiles(files, folderKey, Method.CNC);
//
//        } catch (Exception e) {
//            // 파일 업로드 중 다른 예외 발생 시 처리
//            log.error("파일 업로드 중 오류 발생", e);
//            resultResponse = new ResultResponse(ResultCode.FAILED, e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultResponse);
//        }
//
//        return ResponseEntity.ok(resultResponse);
    }

    // 후처리 (lamda to ecs)
    @PostMapping("/task/complete")
    public void receiveTaskComplete(@RequestBody FileTask fileTask) {
        log.info("Fargate 작업 완료 콜백 수신:");
        log.info("step 저장 파일명: {}", fileTask.getStepName());
        log.info("json 파일명: {}", fileTask.getJsonName());
        log.info("멤버키: {}", fileTask.getMemberKey());
        log.info("출력 경로: {}", fileTask.getJsonOutPath());
        log.info("타입: {}", fileTask.getMethod());

        fileTaskProducer.sendByResult(fileTask);
    }

    @GetMapping("/stream")
    public ResponseEntity<SseEmitter> connect() {
        String clientId = jwtUtil.getAuthPrincipal();
        SseEmitter emitter = new SseEmitter(0L); // 무제한 타임아웃
        sseEmitters.add(clientId, emitter);
        List<JsonNode> tempKeys = filesTaskService.getTempKeys(clientId);
        try {
            emitter.send(SseEmitter.event().name("connect").data(tempKeys));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        sseEmitters.startHeartbeat(emitter); // 하트비트 시작

        return ResponseEntity.ok(emitter);
    }

    @GetMapping("/options") // 옵션 리스트
    public ResponseEntity<List<OptionDTO>> getMaterialOptions() {
        List<OptionDTO> optionList = new ArrayList<>();
        optionList.add(OptionDTO.builder()
                .key("material")
                .options(Material.getMaterialList())
                .build());
        optionList.add(OptionDTO.builder()
                .key("surface")
                .options(Surface.getSurfaceList())
                .build());
        optionList.add(OptionDTO.builder()
                .key("commonDiff")
                .options(CommonDiff.getCommonDiffList())
                .build());
        optionList.add(OptionDTO.builder()
                .key("roughness")
                .options(Roughness.getRoughnessList())
                .build());
        optionList.add(OptionDTO.builder()
                .key("metalMaterialByThickness")
                .options(MetalMaterialByThickness.getAllThicknessListByMaterial())
                .build());
        return ResponseEntity.ok(optionList);
    }

}
