package com.cadify.cadifyWAS;

import com.cadify.cadifyWAS.controller.files.FileController;
import com.cadify.cadifyWAS.model.dto.files.FileTask;
import com.cadify.cadifyWAS.repository.Files.EstimateRepository;
import com.cadify.cadifyWAS.service.file.*;
import com.cadify.cadifyWAS.service.file.common.Method;
import com.cadify.cadifyWAS.service.file.common.cnc.CNCAxisAnalyzer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@SpringBootTest
class CadifyWasApplicationTests {

	@Autowired
	private FilesService filesService;
	@Autowired
	private EstimateService estimateService;
	@Autowired
	private EstimateRepository estimateRepository;
	@Autowired
	private SdkService sdkService;
	@Autowired
	private FilesByFactoryService filesByFactoryService;
	@Autowired
	private GarbageFileService  garbageFileService;
    @Autowired
    private FileController fileController;
    @Autowired
    private FileLogService fileLogService;

	@Test
	void contextLoads() {
	}

	@Test
	void cncTest() throws IOException {
        fileLogService.saveFileUploadFailedLog(
                FileTask.builder()
                        .originFileName("test.step")
                        .stepName("test_20250825.step")
                        .jsonName("test_20250825.json")
                        .method(Method.METAL)
                        .fileSize(12355)
                        .tempKey("tempKey123")
                        .memberKey("memberKey123")
                        .folderKey(null)
                        .jsonOutPath("usr/bin/test_20250825.json")
                        .build()
        , "test error message");
	}


}
