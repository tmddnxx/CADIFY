package com.cadify.cadifyWAS.controller.files;

import com.cadify.cadifyWAS.model.dto.files.FileTask;
import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.file.FilesService;
import com.cadify.cadifyWAS.service.file.rabbitMQ.FileTaskProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file")
@Slf4j
public class FileController {

    private final FilesService filesService;
    private final FileTaskProducer fileTaskProducer;

    // 판금 업로드 처리 메소드
    @PostMapping(value = "/upload/metal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultResponse> uploadFile(@RequestParam("files") List<MultipartFile> files,
                                                     @RequestParam("folderKey") String folderKey) {
        return ResponseEntity.status(500).body(new ResultResponse(ResultCode.FAILED, "현재 서비스 점검중입니다."));
    }

    // 절삭 업로드
    @PostMapping(value = "/upload/cnc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultResponse> uploadCncFile(@RequestParam("files") List<MultipartFile> files,
                                                        @RequestParam("folderKey") String folderKey) {
        return ResponseEntity.status(500).body(new ResultResponse(ResultCode.FAILED, "현재 서비스 점검중입니다."));
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
        return ResponseEntity.ok(filesService.connectSse());
    }

    @GetMapping("/options")
    public ResponseEntity<List<OptionDTO>> getMaterialOptions() {
        return ResponseEntity.ok(filesService.getMaterialOptions());
    }

}
