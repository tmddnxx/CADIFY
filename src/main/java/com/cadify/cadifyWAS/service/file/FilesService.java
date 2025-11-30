package com.cadify.cadifyWAS.service.file;

import com.amazonaws.services.s3.AmazonS3;
import com.cadify.cadifyWAS.model.dto.files.*;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.cadify.cadifyWAS.repository.Files.EstimateRepository;
import com.cadify.cadifyWAS.repository.Files.FolderRepository;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.file.common.CommentType;
import com.cadify.cadifyWAS.service.file.common.Method;
import com.cadify.cadifyWAS.service.file.common.MethodType;
import com.cadify.cadifyWAS.service.file.common.cnc.CNCLimit;
import com.cadify.cadifyWAS.service.file.common.metal.MetalLimit;
import com.cadify.cadifyWAS.service.file.rabbitMQ.FileTaskProducer;
import com.cadify.cadifyWAS.util.JwtUtil;
import com.cadify.cadifyWAS.util.PrivateValue;
import com.cadify.cadifyWAS.service.file.common.FileCommon;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.EnumUtils;
import org.joda.time.DateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.*;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Log4j2
@Service
public class FilesService {

    private final AmazonS3 amazonS3;
    private final EstimateRepository estimateRepository;
    private final FolderRepository folderRepository;
    @Value("${aws.s3.bucket.name}")
    private String bucketName;
    @Value("${aws.s3.bucket.name.image}")
    private String imageBucketName;
    private final JwtUtil jwtUtil;
    private final GarbageFileService garbageFileService;
    private final SdkService sdkService;
    private final PrivateValue privateValue;
    private final FilesTaskService filesTaskService;
    private final FileTaskProducer fileTaskProducer;
    private final EcsClient ecsClient = EcsClient.builder()
            .region(software.amazon.awssdk.regions.Region.AP_NORTHEAST_2)
            .build();
    private final long FILE_SIZE_LIMIT = 5L * 1024 * 1024 * 1024; // 5GB

    // 업로드 요청 보내기 (mq로)
    public ResultResponse uploadFiles(List<MultipartFile> files, String folderKey, Method method) throws IOException {
        String memberKey = jwtUtil.getAuthPrincipal();
        // aa
        List<FilesDTO.StatusResponse> statusResponseList = new ArrayList<>();
        if (files.size() > 20) {
            throw new RuntimeException("파일은 한번에 최대 20개까지 업로드 가능합니다.");
        }
        long totalFileSize = estimateRepository.findTotalFileSize(memberKey); // 총 파일 사이즈
        long uploadTotalFileSize = 0;

        for (MultipartFile file : files) {
            uploadTotalFileSize += file.getSize();
        }

        if (totalFileSize + uploadTotalFileSize > FILE_SIZE_LIMIT) {
            throw new RuntimeException("업로드 용량 초과입니다. \n현재 사용중인 용량 : " + totalFileSize + "\n업로드 용량 : " + uploadTotalFileSize);
        }

        // 이미 처리중인 파일이 50개 이상일 경우 예외처리
        List<JsonNode> tempKeys = filesTaskService.getTempKeys(memberKey);
        int tempKeyCount = tempKeys.size();
        int uploadCount = files.size();
        if (tempKeyCount + uploadCount > 50) {
            throw new RuntimeException("파일은 최대 50개까지 처리 가능합니다. \n잠시후 다시 시도해주세요.\n현재 처리중인 파일 수  :" + tempKeyCount);
        }

        for (MultipartFile file : files) {
            String tempKey = UUID.randomUUID().toString();
            log.info("🟢 처리 시작: {} 파일이름 : {} 파일 크기(byte) : {}" , DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), file.getOriginalFilename(), file.getSize());
            String originFileName = file.getOriginalFilename();

            try {
                FileCommon.validateFile(originFileName, file); // 파일 이름 검증
                statusResponseList.add(FilesDTO.StatusResponse.builder()
                        .fileName(file.getOriginalFilename())
                        .tempKey(tempKey)
                        .fileSize(file.getSize())
                        .message("업로드 성공")
                        .isSuccess(true)
                        .build());
            }catch (Exception e){
                statusResponseList.add(FilesDTO.StatusResponse.builder()
                        .fileName(file.getOriginalFilename())
                        .message(e.getMessage())
                        .isSuccess(false)
                        .build());
                continue;
            }
            LocalDateTime now = LocalDateTime.now();
            String savedFileName = FileCommon.formatFileName(originFileName, now)+".stp";
            String jsonFileName = savedFileName.split(".stp")[0] + ".json";
            String imageFileName = savedFileName.split(".stp")[0] + ".png";

            if(savedFileName.contains(" ")){
                savedFileName = savedFileName.replace(" ", "");
            }
            if(jsonFileName.contains(" ")){
                jsonFileName = jsonFileName.replace(" ", "");
            }
            if (imageFileName.contains(" ")){
                imageFileName = imageFileName.replace(" ", "");
            }

            String basePath = privateValue.getFileRootDir();
            Path dataPath = Paths.get(basePath, String.valueOf(method), "input").resolve(memberKey).resolve(savedFileName);
            Path outPath = Paths.get(basePath, String.valueOf(method), "output").resolve(memberKey).resolve(jsonFileName);
            java.nio.file.Files.createDirectories(dataPath.getParent());
            java.nio.file.Files.createDirectories(outPath.getParent());
            java.nio.file.Files.write(dataPath, file.getBytes());

            FileTask fileTask = FileTask.builder()
                    .originFileName(originFileName)
                    .stepName(savedFileName)
                    .jsonName(jsonFileName)
                    .imageName(imageFileName)
                    .jsonOutPath(outPath.toString())
                    .memberKey(memberKey)
                    .folderKey(folderKey.isEmpty() ? null : folderKey)
                    .tempKey(tempKey)
                    .fileSize(file.getSize())
                    .createdAt(now)
                    .method(method)
                    .build();

            if (method == Method.METAL) {
                fileTaskProducer.sendByMetal(fileTask);
            }else if (method == Method.CNC) {
                fileTaskProducer.sendByCnc(fileTask);
            }

            // redis에 tempkey 저장으로 파일 처리 상태관리
            filesTaskService.saveTempKey(memberKey,
                    FilesDTO.SSEResponse.builder()
                            .fileName(originFileName)
                            .tempKey(tempKey)
                            .folderKey(folderKey)
                            .method(String.valueOf(method))
                            .createdAt(now.toString())
                            .build());
        }

        return ResultResponse.of(ResultCode.SUCCESS, statusResponseList);
    }

    // 판금 후처리하기
    public EstimateDTO.StatusResponse processingMetalResult(FileTask response) {
        String outPutPath = response.getJsonOutPath();
        String memberKey = response.getMemberKey();

        String savedFileName = response.getStepName();
        String imageFileName = response.getImageName();
        String estName = FileCommon.generatedEstName("sheet_metal");

        String originFileName = response.getOriginFileName();
        String folderKey = response.getFolderKey();
        String s3StepFileKey = memberKey + "/metal/step/" + savedFileName; // s3 step 파일 주소
        String s3ImageFileKey = memberKey + "/metal/" + imageFileName; // s3 이미지 파일 주소
        try{
            String metaJson = Files.readString(Paths.get(outPutPath));

            if(metaJson == null){
                throw new RuntimeException("이용 불가능 한 파일입니다.\n고객센터에 문의해주세요.");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(metaJson);
            JsonNode parts = root.get("parts");

            if (parts == null) {
                throw new RuntimeException("이용 불가능 한 파일입니다.\n고객센터에 문의해주세요.");
            }

            // 가공타입 확인
            String type = MetalLimit.extractMetalType(parts);
            if(!EnumUtils.isValidEnum(MethodType.class, type)){
                throw new IllegalArgumentException("가공 불가능한 타입입니다.");
            }

            // MetaJson에서 에러코드 찾기 (있으면 에러)
            List<Integer> warningCode = MetalLimit.extractWarningDetails(parts);

            // MetaJson에서 thickness 찾기
            double thickness = MetalLimit.extractThickness(parts);

            List<Estimate.ErrorDetail> errorDetailList = new ArrayList<>();

            // 에러코드 3208은 제작 요청 가능함
            if (warningCode != null && warningCode.contains(3208)) {
                Estimate.ErrorDetail errorDetail = Estimate.ErrorDetail.builder()
                        .type("3208_REQUIRED")
                        .message("윗면과 아랫면의 면의 면적이 다릅니다. \n윗면과 아랫면의 면적이 같도록 모델링을 수정해 주세요.")
                        .data(EstimateDTO.ErrorFlag
                                .builder()
                                .flag(false)
                                .comment(CommentType.COMMENT_TYPE1)
                                .build()
                        )
                        .build();
                errorDetailList.add(errorDetail);
                warningCode = warningCode.stream()
                        .filter(i -> i != 3208)
                        .toList();
                warningCode = warningCode.isEmpty() ? null : warningCode; // warningCode가 비어있으면 null로 설정
            }

            String sdkErrorCode = warningCode == null ? null : warningCode.toString();

            // MeataJson에서 hole 찾기
            String drillHoles = MetalLimit.extractHole(parts, errorDetailList);
            // MetaJson에서 한계치 찾기
            String limitError= MetalLimit.extractLimitError(parts, thickness, errorDetailList);
            OptionDTO.BBox bbox = MetalLimit.extractBBox(parts);

            String basePath = privateValue.getFileRootDir();

            Path dataPath = Paths.get(basePath, "metal", "stepData").resolve(memberKey);
            Path outPath = Paths.get(basePath, "metal", "jsonOut").resolve(memberKey);
            File fileDir = new File(outPath.toString());
            log.info("파일 이름 : {} ", savedFileName);
            log.info("이미지 이름 : {} ", imageFileName);
            File[] stpFile = Objects.requireNonNull(fileDir.listFiles((d, name) -> name.equals(FileCommon.getColoredSTPName(savedFileName))));
            if (stpFile.length == 0) {
                deleteFilesInDirectory(dataPath, savedFileName.split(".stp")[0]);
                deleteFilesInDirectory(outPath, savedFileName.split(".stp")[0]);
                throw new RuntimeException("파일에 알 수 없는 문제가 있습니다. \n정상적인 솔리드 모델을 업로드 해 주세요");
            }
            log.info("stp 파일 경로 : {}", stpFile[0].getAbsolutePath());
            File imageFile = Objects.requireNonNull(fileDir.listFiles((d, name) -> name.equals((imageFileName))))[0];
            log.info("이미지 파일 경로 : {}", imageFile.getAbsolutePath());


            // S3에 step파일 & 이미지 업로드
            amazonS3.putObject(bucketName, s3StepFileKey, stpFile[0]);
            amazonS3.putObject(imageBucketName, s3ImageFileKey, imageFile);

            String imageUrl = amazonS3.getUrl(imageBucketName, s3ImageFileKey).toString();

            // 파일 저장
            String fileKey = UUID.randomUUID().toString();
            FilesDTO.Post filesDTO = FilesDTO.Post.builder()
                    .fileKey(fileKey)
                    .fileName(savedFileName)
                    .memberKey(memberKey)
                    .s3StepAddress(s3StepFileKey)
                    .imageAddress(imageUrl)
                    .metaJson(metaJson)
                    .build();

            // 견적 저장
            String estKey = UUID.randomUUID().toString();
            EstimateDTO.Post estimateDTO = EstimateDTO.Post.builder()
                    .estKey(estKey)
                    .fileName(originFileName)
                    .memberKey(memberKey)
                    .estName(estName)
                    .folderKey(folderKey)
                    .createdAt(response.getCreatedAt())
                    .errorCode(sdkErrorCode)
                    .method("sheet_metal")
                    .type(type)
                    .thickness(thickness)
                    .holeJson(drillHoles)
                    .erorrDetails(limitError)
                    .fileSize(response.getFileSize())
                    .bbox(objectMapper.writeValueAsString(bbox))
                    .policyVersion(privateValue.getPolicyVersion())
                    .build();

            FilesDTO.KeyResponse keyResponse = filesTaskService.saveEntities(filesDTO, estimateDTO); // 파일 및 견적 저장
            log.info("🟢 처리 완료: {} 파일이름 : {}" , DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), originFileName);
            return EstimateDTO.StatusResponse.builder()
                    .estKey(keyResponse.getEstKey())
                    .fileName(originFileName)
                    .isSuccess(true)
                    .message("업로드 성공")
                    .data(
                            EstimateDTO.Response.builder()
                                    .estKey(keyResponse.getEstKey())
                                    .folderKey(folderKey)
                                    .fileName(originFileName)
                                    .estName(estName)
                                    .type(type)
                                    .imageUrl(imageUrl)
                                    .stepS3(s3StepFileKey)
                                    .method("sheet_metal")
                                    .errorCode(warningCode)
                                    .errorDetails(limitError)
                                    .thickness(thickness)
                                    .holeJson(drillHoles)
                                    .createdAt(response.getCreatedAt())
                                    .tempKey(response.getTempKey())
                                    .fileSize(response.getFileSize())
                                    .bbox(bbox)
                                    .build()
                    )
                    .build();
        }catch (Exception e){
            handleException(e, s3StepFileKey, s3ImageFileKey); // 예외 발생시 s3파일 삭제 및 garbage 테이블에 저장
            throw new RuntimeException(e.getMessage());
        }
    }

    // 절삭 후처리하기
    public EstimateDTO.StatusResponse processingCNCResult(FileTask response) throws JsonProcessingException {
        String outPutPath = response.getJsonOutPath();
        String memberKey = response.getMemberKey();

        String savedFileName = response.getStepName();
        String imageFileName = response.getImageName();
        String estName = FileCommon.generatedEstName("cnc");

        String originFileName = response.getOriginFileName();
        String folderKey = response.getFolderKey();
        String s3StepFileKey = memberKey + "/cnc/step/" + savedFileName; // s3 step 파일 주소
        String s3ImageFileKey = memberKey + "/cnc/" + imageFileName; // s3 이미지 파일 주소


        try{
            String metaJson = Files.readString(Paths.get(outPutPath));

            if(metaJson == null){
                throw new Exception("도커 json 생성 실패");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(metaJson);
            JsonNode parts = root.get("parts");

            if (parts == null) {
                throw new RuntimeException("이용 불가능 한 파일입니다.\n고객센터에 문의해주세요.");
            }

            // 가공타입 확인
            String type = CNCLimit.extractCnCType(parts);
            if(!EnumUtils.isValidEnum(MethodType.class, type)){
                throw new IllegalArgumentException("가공 불가능한 타입입니다.");
            }

            // MetaJson에서 semanticsCode 찾기 (있으면 에러)
            List<Estimate.ErrorDetail> errorDetailList = new ArrayList<>();
            List<Integer> warningCode = CNCLimit.extractWarningDetailsByCnc(parts);

            // 2204 (tx에러)는 제작 요청 가능함
            if (warningCode != null && warningCode.contains(2204)) {
                Estimate.ErrorDetail errorDetail = Estimate.ErrorDetail.builder()
                        .type("2204_REQUIRED")
                        .message("최소 벽 두께가 너무 얇습니다. \n벽 두께를 0.8mm 이상으로 모델링을 수정해 주세요.")
                        .data(EstimateDTO.ErrorFlag
                                .builder()
                                .flag(false)
                                .comment(CommentType.COMMENT_TYPE4)
                                .build()
                        )
                        .build();
                errorDetailList.add(errorDetail);
                warningCode = warningCode.stream()
                        .filter(i -> i != 2204)
                        .toList();
                warningCode = warningCode.isEmpty() ? null : warningCode; // warningCode가 비어있으면 null로 설정
            }

            String sdkErrorCode = warningCode == null ? null : warningCode.toString();
            String holeJson = CNCLimit.extractHoles(parts, errorDetailList);
            String limitError = CNCLimit.extractLimit(parts, type, errorDetailList); // 한계치 검사
            OptionDTO.BBox bbox = CNCLimit.extractBBox(parts);

            String basePath = privateValue.getFileRootDir();
            Path dataPath = Paths.get(basePath, "cnc", "stepData").resolve(memberKey);
            Path outPath = Paths.get(basePath, "cnc", "jsonOut").resolve(memberKey);
            File fileDir = new File(outPath.toString());
            log.info("파일 이름 : {} ", savedFileName);
            log.info("이미지 이름 : {} ", imageFileName);
            File[] stpFile = Objects.requireNonNull(fileDir.listFiles((d, name) -> name.equals(FileCommon.getColoredSTPName(savedFileName))));
            if (stpFile.length == 0) {
                deleteFilesInDirectory(dataPath, savedFileName.split(".stp")[0]);
                deleteFilesInDirectory(outPath, savedFileName.split(".stp")[0]);
                throw new RuntimeException("파일에 알 수 없는 문제가 있습니다. \n정상적인 솔리드 모델을 업로드 해 주세요");
            }
            log.info("stp 파일 경로 : {}", stpFile[0].getAbsolutePath());
            File imageFile = Objects.requireNonNull(fileDir.listFiles((d, name) -> name.equals(imageFileName)))[0];
            log.info("이미지 파일 경로 : {}", imageFile.getAbsolutePath());


            // S3에 step파일 업로드
            amazonS3.putObject(bucketName, s3StepFileKey, stpFile[0]);
            amazonS3.putObject(imageBucketName, s3ImageFileKey, imageFile);

            String imageUrl = amazonS3.getUrl(imageBucketName, s3ImageFileKey).toString();

            // 파일 저장
            String fileKey = UUID.randomUUID().toString();
            FilesDTO.Post filesDTO = FilesDTO.Post.builder()
                    .fileKey(fileKey)
                    .fileName(savedFileName)
                    .memberKey(memberKey)
                    .s3StepAddress(s3StepFileKey)
                    .imageAddress(imageUrl)
                    .metaJson(metaJson)
                    .build();

            // 견적 저장
            String estKey = UUID.randomUUID().toString();
            EstimateDTO.Post estimateDTO = EstimateDTO.Post.builder()
                    .estKey(estKey)
                    .fileName(originFileName)
                    .memberKey(memberKey)
                    .estName(estName)
                    .folderKey(folderKey)
                    .createdAt(response.getCreatedAt())
                    .errorCode(sdkErrorCode)
                    .method("cnc")
                    .type(type)
                    .thickness(0)
                    .holeJson(holeJson)
                    .erorrDetails(limitError)
                    .fileSize(response.getFileSize())
                    .bbox(objectMapper.writeValueAsString(bbox))
                    .policyVersion(privateValue.getPolicyVersion())
                    .build();

            FilesDTO.KeyResponse keyResponse = filesTaskService.saveEntities(filesDTO, estimateDTO); // 파일 및 견적 저장
            log.info("🟢 처리 완료: {} 파일이름 : {}" , DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), originFileName);
            return EstimateDTO.StatusResponse.builder()
                    .estKey(keyResponse.getEstKey())
                    .fileName(originFileName)
                    .isSuccess(true)
                    .message("업로드 성공")
                    .data(
                            EstimateDTO.Response.builder()
                                    .estKey(keyResponse.getEstKey())
                                    .fileName(originFileName)
                                    .estName(estName)
                                    .type(type)
                                    .imageUrl(imageUrl)
                                    .stepS3(s3StepFileKey)
                                    .method("cnc")
                                    .errorCode(warningCode)
                                    .createdAt(response.getCreatedAt())
                                    .folderKey(folderKey)
                                    .errorDetails(limitError)
                                    .holeJson(holeJson)
                                    .tempKey(response.getTempKey())
                                    .fileSize(response.getFileSize())
                                    .bbox(bbox)
                                    .build()
                    )
                    .build();
        }catch (Exception e){
            handleException(e, s3StepFileKey, s3ImageFileKey); // 예외 발생시 s3파일 삭제 및 garbage 테이블에 저장
            throw new RuntimeException(e.getMessage());
        }finally {
            filesTaskService.removeTempKey(memberKey, response.getTempKey()); // 처리 완료된 tempKey 제거
        }
    }

    // 태스크 실행 요청
    public void executeTask(FileTask task, Method method)  {
        String clusterName = privateValue.getClusterName(); // 필요 시 변경
        log.info("메소드 : {} ", method);
        String taskDefinition = method == Method.METAL ?
                privateValue.getMetalTaskDefinitionName() :
                privateValue.getCncTaskDefinitionName(); // 메탈과 CNC에 따라 태스크 정의 변경
        String stepFile = task.getStepName();
        String jsonFile = task.getJsonName();
        String imageFile = task.getImageName();
        String memberKey = task.getMemberKey(); // 실제 memberKey 입력

        RunTaskRequest request = RunTaskRequest.builder()
                .cluster(clusterName)
                .launchType(LaunchType.FARGATE)
                .taskDefinition(taskDefinition)
                .networkConfiguration(NetworkConfiguration.builder()
                        .awsvpcConfiguration(AwsVpcConfiguration.builder()
                                .assignPublicIp(AssignPublicIp.ENABLED)
                                .subnets(privateValue.getTaskSubnetId()) // 실제 subnet ID 입력
                                .securityGroups(privateValue.getTaskSecurityGroupId()) // 실제 SG 입력
                                .build())
                        .build())
                .overrides(TaskOverride.builder()
                        .containerOverrides(ContainerOverride.builder()
                                .name(privateValue.getTaskContainerName()) // task 정의의 컨테이너 이름
                                .environment(
                                        KeyValuePair.builder().name("STEP_FILE").value(stepFile).build(),
                                        KeyValuePair.builder().name("JSON_FILE").value(jsonFile).build(),
                                        KeyValuePair.builder().name("IMAGE_FILE").value(imageFile).build(),
                                        KeyValuePair.builder().name("MEMBER_KEY").value(memberKey).build(),
                                        KeyValuePair.builder().name("OUT_PATH").value(task.getJsonOutPath()).build(),
                                        KeyValuePair.builder().name("ORIGIN_FILE_NAME").value(task.getOriginFileName()).build(),
                                        KeyValuePair.builder().name("FOLDER_KEY").value(task.getFolderKey()).build(),
                                        KeyValuePair.builder().name("TEMP_KEY").value(task.getTempKey()).build(),
                                        KeyValuePair.builder().name("FILE_SIZE").value(String.valueOf(task.getFileSize())).build(),
                                        KeyValuePair.builder().name("CREATED_AT").value(String.valueOf(task.getCreatedAt())).build(),
                                        KeyValuePair.builder().name("METHOD").value(String.valueOf(task.getMethod())).build()
                                )
                                .build())
                        .build())
                .build();

        RunTaskResponse response = ecsClient.runTask(request);

        System.out.println("Started task: " + response.tasks());
        if (!response.failures().isEmpty()) {
            System.err.println("Failures: " + response.failures());
        }
    }
    // ----------------------------------------------------------------------------------------

    // 폴더 내 파일 clean
    public void cleanUpDirectory(FileTask task) throws IOException{
        String basePath = privateValue.getFileRootDir();
        String stpName = task.getStepName().split(".stp")[0];
        String outName = task.getJsonName().split(".json")[0];
        Path dataPath = Paths.get(basePath, String.valueOf(task.getMethod()), "input").resolve(task.getMemberKey());
        deleteFilesInDirectory(dataPath, stpName);
        Path outPath = Paths.get(basePath, String.valueOf(task.getMethod()), "output").resolve(task.getMemberKey());
        deleteFilesInDirectory(outPath, outName);
        log.info("✅ 파일 다 지웠음");
    }

    private void deleteFilesInDirectory(Path directory, String fileName) throws IOException {
        List<Path> fileToDelete = new ArrayList<>();

        try (DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (java.nio.file.Files.isDirectory(entry)) {
                    continue;
                }
                String entryName = entry.getFileName().toString();
                String normalizedEntryName = FileCommon.replaceSpaceAndDot(entryName);
                if (normalizedEntryName.contains(FileCommon.replaceSpaceAndDot(fileName))) {
                    fileToDelete.add(entry);
                }
            }
        }

        for (Path path : fileToDelete) {
            java.nio.file.Files.delete(path);
        }
    }

    // 예외 처리 (파일 삭제 및 롤백)
    private void handleException(Exception e, String s3StepFileKey, String s3ImageFileKey) {
        log.info("파일 업로드 예외 발생 : {}", e.getMessage());
        GarbageFilesDTO stepGarbage = GarbageFilesDTO.builder()
                .bucket(bucketName)
                .path(s3StepFileKey)
                .build();
        GarbageFilesDTO imageGarbage = GarbageFilesDTO.builder()
                .bucket(imageBucketName)
                .path(s3ImageFileKey)
                .build();

        amazonS3.deleteObject(bucketName, s3StepFileKey); // db 롤백시 s3 step도 롤백
        amazonS3.deleteObject(imageBucketName, s3ImageFileKey); // db 롤백시 s3 step도 롤백
        garbageFileService.saveGarbageFiles(stepGarbage); // garbage 테이블에 저장
        garbageFileService.saveGarbageFiles(imageGarbage); // garbage 테이블에 저장
    }

}
