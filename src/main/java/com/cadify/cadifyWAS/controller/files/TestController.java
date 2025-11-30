package com.cadify.cadifyWAS.controller.files;

import com.cadify.cadifyWAS.config.SseEmitters;
import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.model.dto.files.FileTask;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.file.*;
import com.cadify.cadifyWAS.service.file.common.Method;
import com.cadify.cadifyWAS.service.file.rabbitMQ.FileTaskProducer;
import com.cadify.cadifyWAS.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/file/test")
public class TestController {

    private final FilesService filesService;
    private final JwtUtil jwtUtil;
    private final SseEmitters sseEmitters;
    private final FilesTaskService filesTaskService;
    private final long MAXSIZE = 2 * 1024 * 1024; // 파일 크기 제한을 2MB로 설정
    private final long TOTALMAXSIZE = 40 * 1024 * 1024; // 파일 크기 제한을 40MB로 설정
    private final TestService testService;
    private final FileTaskProducer fileTaskProducer;




}
