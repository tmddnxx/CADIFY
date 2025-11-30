package com.cadify.cadifyWAS.service.file;


import com.amazonaws.services.s3.AmazonS3;
import com.cadify.cadifyWAS.controller.files.TestController;
import com.cadify.cadifyWAS.model.dto.files.*;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.cadify.cadifyWAS.repository.Files.EstimateRepository;
import com.cadify.cadifyWAS.repository.Files.FilesRepository;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.file.common.CommentType;
import com.cadify.cadifyWAS.service.file.common.FileCommon;
import com.cadify.cadifyWAS.service.file.common.Method;
import com.cadify.cadifyWAS.service.file.common.MethodType;
import com.cadify.cadifyWAS.service.file.common.metal.MetalLimit;
import com.cadify.cadifyWAS.service.file.rabbitMQ.FileTaskProducer;
import com.cadify.cadifyWAS.util.JwtUtil;
import com.cadify.cadifyWAS.util.PrivateValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.EnumUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class TestService {

    private final SdkService sdkService;
    private final EstimateRepository estimateRepository;
    private final FilesRepository filesRepository;
    private final FilesTaskService filesTaskService;
    private final JwtUtil jwtUtil;
    private final PrivateValue privateValue;
    private final FileTaskProducer fileTaskProducer;
    private final EcsClient ecsClient = EcsClient.builder()
            .region(software.amazon.awssdk.regions.Region.AP_NORTHEAST_2)
            .build();
    private final AmazonS3 amazonS3;
    @Value("${aws.s3.bucket.name}")
    private String bucketName;
    @Value("${aws.s3.bucket.name.image}")
    private String imageBucketName;
    private final long FILE_SIZE_LIMIT = 5L * 1024 * 1024 * 1024; // 5GB
    private final GarbageFileService garbageFileService;



}
