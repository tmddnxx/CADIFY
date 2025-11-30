package com.cadify.cadifyWAS.service.file;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.mapper.EstimateMapper;
import com.cadify.cadifyWAS.model.dto.files.CostDTO;
import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.model.dto.files.GarbageFilesDTO;
import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.cadify.cadifyWAS.model.entity.Files.Files;
import com.cadify.cadifyWAS.model.entity.Files.Folder;
import com.cadify.cadifyWAS.repository.Files.EstimateRepository;
import com.cadify.cadifyWAS.repository.Files.FilesRepository;
import com.cadify.cadifyWAS.repository.Files.FolderRepository;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.factory.KFactorService;
import com.cadify.cadifyWAS.service.file.common.EstimateStatus;
import com.cadify.cadifyWAS.service.file.common.cnc.CNCAxisAnalyzer;
import com.cadify.cadifyWAS.service.file.common.cnc.CNCLimit;
import com.cadify.cadifyWAS.service.file.common.cnc.CNCPrice;
import com.cadify.cadifyWAS.service.file.common.metal.MetalLimit;
import com.cadify.cadifyWAS.service.file.common.metal.MetalPrice;
import com.cadify.cadifyWAS.service.file.enumValues.common.CommonDiff;
import com.cadify.cadifyWAS.service.file.enumValues.common.Roughness;
import com.cadify.cadifyWAS.service.file.enumValues.common.Shipment;
import com.cadify.cadifyWAS.service.file.enumValues.metal.defaultValue.MetalShipment;
import com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.material.MetalMaterialByThickness;
import com.cadify.cadifyWAS.service.file.rabbitMQ.LogService;
import com.cadify.cadifyWAS.service.orchestrator.EstimateCartFacade;
import com.cadify.cadifyWAS.util.JwtUtil;
import com.cadify.cadifyWAS.service.file.common.FileCommon;
import com.cadify.cadifyWAS.util.PrivateValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@Log4j2
@RequiredArgsConstructor
public class EstimateService {

    private final AmazonS3 amazonS3;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final PrivateValue privateValue;
    @Value("${aws.s3.bucket.name}")
    private String bucketName;
    @Value("${aws.s3.bucket.name.image}")
    private String imageBucketName;
    private final EstimateRepository estimateRepository;
    private final EstimateMapper estimateMapper;
    private final GarbageFileService garbageFileService;
    private final EntityManager entityManager;
    private final FilesRepository filesRepository;
    private final FolderRepository folderRepository;
    private final CNCAxisAnalyzer cncAxisAnalyzer;

    private final LogService logService; // 테스트용 로그 서비스
    
    // 견적 리스트 불러오기
    public EstimateDTO.ListResponse estimateList(String memberKey, String folderKey){

        try {
            // 페이지 요청 정의
            Pageable pageable = PageRequest.of(0, 1000);

            // 데이터 조회
            Page<Tuple> estimatePage = estimateRepository.findEstimateWithFile(memberKey, folderKey,pageable);
            long totalFileSize = estimateRepository.findTotalFileSize(memberKey);

            // DTO로 변환
            Page<EstimateDTO.Response> responsePage = estimatePage.map(estimate -> {
                // Object 배열에서 첫 번째 값은 Estimate 객체, 두번째는 값은 이미지주소

                Estimate estimateEntity = (Estimate) estimate.get(0);
                String imageUrl = (String) estimate.get(1);

                EstimateDTO.Response response = null;
                try {
                    response = estimateMapper.estimateToEstimateResponse(estimateEntity);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                response.setImageUrl(imageUrl);

                // DTO로 변환하여 반환
                return response;
            });

            return EstimateDTO.ListResponse.builder()
                    .response(responsePage.getContent())
                    .totalFileSize(totalFileSize)
                    .build();
        } catch (Exception e) {
            // 기타 예외 처리
            throw new RuntimeException("견적 리스트를 불러오는 중 문제가 발생했습니다.", e);
        }
    }

    // 단일 견적 불러오기 ( json 포함 )
    public EstimateDTO.Response getEstimate(String estKey){
        try {
            String memberKey = jwtUtil.getAuthPrincipal();
            // 리포지토리에서 데이터 조회
            List<Tuple> joinData = estimateRepository.findEstimateWithFileJson(estKey, memberKey);

            // 데이터가 없는 경우
            if (joinData == null || joinData.isEmpty()) {
                throw new IllegalArgumentException("견적 데이터를 찾을 수 없습니다.");
            }

            Tuple tuple = joinData.get(0);

            // Tuple 데이터 가져오기
            Estimate estimate = tuple.get(0, Estimate.class);  // 첫 번째 컬럼
            String s3Step = tuple.get(1, String.class);
            Files files = filesRepository.findById(estimate.getFileId()).orElseThrow(() -> new Exception("파일 정보를 찾을 수 없습니다."));
            String dxfAddress = files.getS3DxfAddress(); // 멤버키/metal/dxf/파일이름
            String dxfName = FileCommon.extractDxfFileName(dxfAddress);

            // 데이터 변환
            EstimateDTO.Response response = estimateMapper.estimateToEstimateResponse(estimate);

            response.setStepS3(s3Step);
            response.setDxfName(dxfName);

            return response;
        } catch (Exception e) {
            // 그 외의 예외 처리
            throw new RuntimeException("견적 데이터를 불러오는 중 오류가 발생했습니다.");
        }
    }

    // 파일이름 변경
    @Transactional
    public EstimateDTO.StatusResponse patchFileName(String estKey, String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new RuntimeException("파일 이름이 비어있습니다.");
        }

        fileName = fileName.trim();

        String originFileName;
        if (fileName.contains(".")) {
            // 확장자가 포함된 경우
            if (!fileName.toLowerCase().endsWith(".step")) {
                throw new RuntimeException("확장자는 .step만 허용됩니다.");
            }
            originFileName = fileName;
        } else {
            // 확장자가 없는 경우 .step 추가
            originFileName = fileName + ".step";
        }

        // .step 확장자 제거한 이름 추출 후 정규화
        String baseFileName = originFileName.substring(0, originFileName.toLowerCase().lastIndexOf(".step"));
        String normalizedFileName = Normalizer.normalize(baseFileName, Normalizer.Form.NFC);

        // 유효한 문자 검증
        if (!normalizedFileName.matches("^[a-zA-Z0-9가-힣ㄱ-ㅎ _.,~-]+$")) {
            throw new RuntimeException("파일이름에 특수문자는 사용 불가합니다.");
        }

        // 전체 파일명 길이 검증
        if (originFileName.length() > 255) {
            throw new RuntimeException("파일 이름은 255자를 초과할 수 없습니다.");
        }

        Optional<Estimate> estimate = estimateRepository.findByEstKey(estKey);
        if (estimate.isEmpty()) {
            throw new RuntimeException("정보가 없습니다.");
        }

        try {
            Estimate estimateEntity = estimate.get();
            estimateEntity.updateFileName(originFileName);
            estimateRepository.save(estimateEntity);
        } catch (Exception e) {
            throw new RuntimeException("서버 업데이트 에러");
        }

        return EstimateDTO.StatusResponse.builder()
                .estKey(estKey)
                .fileName(originFileName)
                .isSuccess(true)
                .message("파일이름 변경 완료")
                .build();
    }


    // 판금 견적 옵션 업데이트
    @Transactional
    public EstimateDTO.StatusResponse putOption(EstimateDTO.MetalOptionPut optionPut) {

        Estimate estimate = estimateRepository.findByEstKey(optionPut.getEstKey())
                .orElseThrow(() -> new RuntimeException(String.valueOf(ExceptionCode.ESTIMATE_NOT_FOUND)));

        String metaJson = estimateRepository.findMetaJsonByEstkey(optionPut.getEstKey());

        if(estimate.getErrorCode() != null){
            throw new RuntimeException("설계 오류가 있는 모델링은 견적을 확인할 수 없습니다.");
        }

        String existErrorJson = estimate.getErrorJson();
        boolean isRequestable = true;

        try {
            if (existErrorJson != null) {
                JsonNode errorJsonNode = objectMapper.readTree(existErrorJson);
                for (JsonNode error : errorJsonNode) {
                    JsonNode flag = error.path("data").path("flag");
                    if (flag.isMissingNode() || !flag.asBoolean()) {
                        isRequestable = false; // flag가 없거나 false인 경우 요청 불가
                        break;
                    }
                }
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("오류 JSON 파싱 중 오류 발생: " + e.getMessage());
        }

        if (!isRequestable) {
            throw new RuntimeException("설계 오류가 있는 모델링은 견적을 확인할 수 없습니다.");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = objectMapper.readTree(metaJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("메타데이터 JSON 파싱 중 오류 발생: " + e.getMessage());
        }

        JsonNode partsArray = root.get("parts");

        double thickness = estimate.getThickness(); // 두께
        String material = optionPut.getMaterial(); // 재질
        String surface = optionPut.getSurface(); // 표면처리

        /* -------------------------------- 한계치 에러 내용 찾기 --------------------------------------------------*/ 
        // 두께별 가능 재질 판단 (안돼면 throw error)
        if(!MetalMaterialByThickness.getMaterialByThickness(material, thickness)){
            throw new RuntimeException("선택하신 재질 '" + material + "'은(는) " + thickness + "mm 두께에서 사용할 수 없습니다.");
        }

        // 납기일 확인
        OptionDTO.ShipmentDayDTO shipmentDayDTO = Shipment.getShipmentDayByMetal(material, surface);
        
        // 에러내용 담을 List
        List<Estimate.ErrorDetail> errorDetailList = FileCommon.convertStringToErrorList(estimate.getErrorJson()) == null ?
                new ArrayList<>() : FileCommon.convertStringToErrorList(estimate.getErrorJson());

        String optionHoleJson = optionPut.getHoleJson();
        String originHoleJson = estimate.getHoleJson();

        boolean isTapSuccess = false;

        try {
            if(originHoleJson != null){
                // true면 optionHoleJson 업데이트, false면 업데이트 안함
                isTapSuccess = MetalLimit.checkTapSizeByMaterialAndThickness(optionHoleJson, thickness, material, errorDetailList);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("홀 크기 체크 중 오류 발생: " + e.getMessage());
        }

        String originMaterial = material;
        // 한계치 체크용 대분류 재질
        String majorMaterial = switch (material) {
            case "SPCC", "SPHC", "SS400", "SECC" -> "steel";
            case "SUS304_No1", "SUS304_2B", "SUS304_SH", "SUS304_SP", "SUS304_DP" -> "stain";
            case "AL5052" -> "al";
            default -> material;
        };

        // 리프팅 홀 확인하기
        // 없음, 전해연마 이 외 홀 검사 필요
        if(!surface.equals("NA") && !surface.equals("EPP")){
            log.info("도장 도금 홀 검사 필요");
            String msg = MetalLimit.findLiftHole(partsArray, majorMaterial);
            if(msg != null){
                throw new RuntimeException(msg);
            }
        }

        // 가격 산정 DTO
        CostDTO.MetalCostDTO costDTO = CostDTO.MetalCostDTO.builder()
                .material(originMaterial)
                .thickness(thickness)
                .kg(0.0)
                .totalLength(0.0)
                .bendCost(0)
                .rBendCost(0)
                .surface(optionPut.getSurface())
                .metalType("")
                .isFastShipment(optionPut.isFastShipment())
                .holeJson(optionHoleJson)
                .build();

        // 한계치 찾기
        String errorJson = MetalLimit.findLimitError(partsArray, thickness, originMaterial, majorMaterial, errorDetailList, costDTO);
        /* -------------------------------- 한계치 에러 내용 찾기 끝 --------------------------------------------------*/

        // 무게 저장
        optionPut.setKg(costDTO.getKg());

        /* -------------------------------- 가격 구하기 --------------------------------------------------*/

        // 가격 산정
        double totalCost = MetalPrice.getPriceCalc(costDTO); // 납기일 제외 총 가격
        int price = 0;

        try{
            estimate.updateMetalOptions(optionPut);
            if(isTapSuccess) estimate.updateHoleJson(optionHoleJson);
            estimate.updateErrorJson(errorJson);
            estimate.updateShipmentDate(shipmentDayDTO.getStandardDay(), shipmentDayDTO.getExpressDay());
            price = setPrice(estimate, optionPut.isFastShipment(), totalCost); // 가격 저장
            estimateRepository.save(estimate);
        }catch (Exception e){
            throw new RuntimeException("서버 업데이트 에러");
        }

        return EstimateDTO.StatusResponse.builder()
                .estKey(optionPut.getEstKey())
                .isSuccess(true)
                .message("옵션 변경 완료")
                .data(price)
                .build();
    }

    // 절삭 견적 옵션 업데이트
    @Transactional
    public EstimateDTO.StatusResponse putCncOption(EstimateDTO.CnCOptionPut optionPut) {

        Estimate estimate = estimateRepository.findByEstKey(optionPut.getEstKey())
                .orElseThrow(() -> new RuntimeException(String.valueOf(ExceptionCode.ESTIMATE_NOT_FOUND)));

        String metaJson = estimateRepository.findMetaJsonByEstkey(optionPut.getEstKey());

        if(estimate.getErrorCode() != null){
            throw new RuntimeException("설계 오류가 있는 모델링은 견적을 확인할 수 없습니다.");
        }

        String existErrorJson = estimate.getErrorJson();
        boolean isRequestable = true;

        if (existErrorJson != null) {
            try {
                JsonNode errorJsonNode = objectMapper.readTree(existErrorJson);
                for (JsonNode error : errorJsonNode) {
                    JsonNode flag = error.path("data").path("flag");
                    if (flag.isMissingNode() || !flag.asBoolean()) {
                        isRequestable = false; // flag가 없거나 false인 경우 요청 불가
                        break;
                    }
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("오류 JSON 파싱 중 오류 발생: " + e.getMessage());
            }
        }

        if (!isRequestable) {
            throw new RuntimeException("설계 오류가 있는 모델링은 견적을 확인할 수 없습니다.");
        }

        // 공차 옵션 or 표면거칠기 옵션 선택 시 dxf 필수 체크
        if (optionPut.getCommonDiff().equals(CommonDiff.D2.getValue()) || optionPut.getRoughness().equals(Roughness.R1.getValue())) {
            Files files = filesRepository.findById(estimate.getFileId())
                    .orElseThrow(() -> new RuntimeException("파일 정보를 찾을 수 없습니다."));
            if (files.getS3DxfAddress() == null) {
                throw new RuntimeException("해당 공차옵션과 표면거칠기 옵션은 dxf 파일이 필수입니다. \ndxf 파일을 업로드해주세요.");
            }
        }

        // 납기일 확인
        OptionDTO.ShipmentDayDTO shipmentDayDTO = Shipment.getShipmentDayByCNC(optionPut.getMaterial(), optionPut.getSurface(), estimate.getType());

        if (shipmentDayDTO.getExpressDay() == null) {
            optionPut.setFastShipment(false);
        }

        String optionHoleJson = optionPut.getHoleJson();
        String originHoleJson = estimate.getHoleJson();

        boolean isTapSuccess = false;
        if(originHoleJson != null){
            // true면 optionHoleJson 업데이트, false면 업데이트 안함
            isTapSuccess = CNCLimit.checkTapHole(optionHoleJson);
        }

        // 절삭 분석 결과
        CNCAxisAnalyzer.AnalysisResult result = cncAxisAnalyzer.analyzeJsonData(metaJson);
        CNCAxisAnalyzer.Result analysisResult =  CNCAxisAnalyzer.printResults(result);
        log.info("결과 객체 값 : {}", analysisResult);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = objectMapper.readTree(metaJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("메타데이터 JSON 파싱 중 오류 발생: " + e.getMessage());
        }
        JsonNode parts = root.get("parts");
        JsonNode body = parts.get(0).get("bodies").get(0);

        double originKg = CNCLimit.calcTotalVolumeKg(body, optionPut.getMaterial());
        optionPut.setKg(originKg);

        Double kg = CNCLimit.extractKg(body, optionPut.getMaterial());

        /* ---------- 한계치 찾기 ---------------*/
        // 리프팅 홀 확인하기
        // 없음, 전해연마 이 외 홀 검사 필요
        if(!optionPut.getSurface().equals("NA") && !optionPut.getSurface().equals("EPP")){
            log.info("도장 도금 홀 검사 필요");
            String msg = CNCLimit.findLiftHole(parts, optionPut.getMaterial());
            if(msg != null){
                throw new RuntimeException(msg);
            }
        }

        List<Estimate.ErrorDetail> errorDetailList = FileCommon.convertStringToErrorList(estimate.getErrorJson()) == null ?
                new ArrayList<>() : FileCommon.convertStringToErrorList(estimate.getErrorJson());
        String limitError = CNCLimit.findLimitError(parts, estimate.getType(), optionPut, errorDetailList);

        logService.add("무게(1.5) : " + kg); // fixme: 테스트용
        logService.add("totalVolume 무게 : " + originKg); // fixme: 테스트용

        log.info("무게 : {}", kg);
        CostDTO.CnCCostDTO costDTO = CostDTO.CnCCostDTO.builder()
                .bodyJson(body)
                .material(optionPut.getMaterial())
                .cncType(estimate.getType())
                .kg(kg)
                .surface(optionPut.getSurface())
                .commonDiff(optionPut.getCommonDiff())
                .roughness(optionPut.getRoughness())
                .isFastShipment(optionPut.isFastShipment())
                .build();

        double totalCost = CNCPrice.getPriceByCnc(costDTO, analysisResult, logService); // 납기일 제외 총 가격 // fixme: 테스트용 로그

        int price = 0;

        try{
            estimate.updateCnCOptions(optionPut);
            if(isTapSuccess) estimate.updateHoleJson(optionHoleJson);
            estimate.updateErrorJson(limitError);
            estimate.updateShipmentDate(shipmentDayDTO.getStandardDay(), shipmentDayDTO.getExpressDay());
            price = setPrice(estimate, optionPut.isFastShipment(), totalCost); // 가격 저장
            estimateRepository.save(estimate);
        }catch (Exception e){
            throw new RuntimeException("서버 업데이트 에러");
        }

        Map<String , Object> data = new HashMap<>();
        data.put("price", price);
        data.put("Logs", logService.getLogs()); // 테스트용 로그 서비스

        return EstimateDTO.StatusResponse.builder()
                .estKey(optionPut.getEstKey())
                .isSuccess(true)
                .message("옵션 변경 완료")
                .data(data)
                .build();
    }

    private int setPrice(Estimate estimate, boolean isFastShipment, double totalCost) {
        int cost = 0;
        int price = 0;
        int otherCost = 0;
        int otherPrice = 0;

        if(isFastShipment){ // 단납기
            cost = (int) Math.round(totalCost * 1.2); // 원가 (공장 정산가 VAT 별도)
            otherCost = (int) Math.round(totalCost); // 표준납기일때 원가
            price = (int) Math.round(cost * 1.3); // 판매가 (고객 판매가 VAT 별도)
            otherPrice = (int) Math.round(otherCost * 1.3); // 표준납기일때 가격
        }else{ // 표준납기
            cost = (int) Math.round(totalCost); // 원가 (공장 정산가 VAT 별도)
            otherCost = (int) Math.round(totalCost * 1.2); // 단납기일때 원가
            price = (int) Math.round(cost * 1.3); // 판매가 (고객 판매가 VAT 별도)
            otherPrice = (int) Math.round(otherCost * 1.3); // 단납기일때 가격
        }

        estimate.updatePrice(price, cost, otherPrice);

        return price;
    }

    // 뷰어에서 dxf 파일 업로드
    @Transactional(rollbackFor = Exception.class)
    public EstimateDTO.StatusResponse patchDxf(String memberKey, String estKey, MultipartFile file) throws Exception {
        try {
            Estimate estimate = estimateRepository.findByEstKey(estKey).orElseThrow(() -> new Exception("견적 정보를 찾을 수 없습니다."));
            Long fileKey = estimate.getFileId();
            if(!Objects.equals(estimate.getMemberKey(), memberKey)) throw new Exception("사용자가 일치하지 않습니다.");
            Files files = filesRepository.findById(fileKey).orElseThrow(() -> new Exception("파일 정보를 찾을 수 없습니다."));
            // dxf url 찾기 (memberKey /metal / dxf / 파일이름 . dxf )
            String existDxfUrl = null;
            // 판금인지 절삭인지 확인
            String method = estimate.getMethod().equals("sheet_metal") ? "/metal" : "/cnc";

            if(StringUtils.hasText(files.getS3DxfAddress())){
                // 새로운 형식으로 S3 Key에서 URL을 추출
                // 예: memberKey/method/dxf/owner/파일이름_info_타임스탬프.확장자
                String expectedKeyPrefix = memberKey + method + "/dxf/owner/";

                // S3 Key가 지정된 형식과 일치하는지 확인
                if (files.getS3DxfAddress().startsWith(expectedKeyPrefix)) {
                    // 파일이름을 포함한 S3 Key를 그대로 existDxfUrl에 저장
                    existDxfUrl = files.getS3DxfAddress();
                } else {
                    log.info("기존 dxf url 못찾음");
                }

                log.info("기존 dxf url : {}", existDxfUrl);
            }

            String originFileName = file.getOriginalFilename();

            if(file.isEmpty()){
                throw new Exception("파일을 업로드해주세요.");
            }

            String lowerName = originFileName.toLowerCase();
            if (!lowerName.endsWith(".dxf") && !lowerName.endsWith(".dwg")) {
                throw new Exception("파일 확장자는 dxf 또는 dwg만 가능합니다.");
            }

            // 확장자 제거
            String baseName = originFileName.replaceAll("\\.[^.]+$", ""); // 확장자 없는 이름

            String normalizedFileName = Normalizer.normalize(baseName, Normalizer.Form.NFC);

            if (!normalizedFileName.matches("^[a-zA-Z0-9가-힣 _.,~-]+$")) {
                throw new Exception("파일이름에 특수문자는 사용 불가합니다.");
            }

            String extension = "";

            Pattern pattern = Pattern.compile("\\.([^.]+)$");  // 마지막 . 이후 확장자
            Matcher matcher = pattern.matcher(originFileName);

            if (matcher.find()) {
                extension = "."+matcher.group(1);  // dxf or dwg
            }

            LocalDateTime now = LocalDateTime.now();
            String savedFileName = FileCommon.formatFileName(baseName, now) + extension;
            String s3DxfFileKey = memberKey + method + "/dxf/owner/" + savedFileName;

            try{
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                InputStream inputStream = new ByteArrayInputStream(file.getBytes());

                // S3에 DXF파일 업로드
                amazonS3.putObject(bucketName, s3DxfFileKey, inputStream, metadata);

                // 업로드 성공시
                files.updateS3DxfAddress(s3DxfFileKey);

                try{
                    // 성공하면 기존 파일 s3에서 삭제
                    if(existDxfUrl != null){
                        amazonS3.deleteObject(bucketName, existDxfUrl);
                    }
                }catch (Exception e){
                    // 기존 파일 삭제 실패시에도 저장
                    GarbageFilesDTO exist = new GarbageFilesDTO();
                    exist.setPath(existDxfUrl); // 실패한 step파일 경로 저장
                    garbageFileService.saveGarbageFiles(exist); // garbage 테이블에 저장
                }

                
            }catch (Exception e) { // 파일 DB, S3 업로드에서 예외발생
                log.info("뷰어에서 DXF 업로드 예외 발생 : {} ",e.getMessage());
                amazonS3.deleteObject(bucketName, s3DxfFileKey); // db 롤백시 s3도 롤백
                // 실패하면 배치돌리기 위해 저장
                GarbageFilesDTO garbageFilesDTO = new GarbageFilesDTO();
                garbageFilesDTO.setPath(s3DxfFileKey); // 실패한 파일 경로 저장
                garbageFileService.saveGarbageFiles(garbageFilesDTO); // garbage 테이블에 저장

                throw new Exception(e.getMessage());
            }
        } catch (Exception e){ // 파일읽기에서 오류 발생
            throw new Exception(e.getMessage());
        }

        return EstimateDTO.StatusResponse.builder()
                .estKey(estKey)
                .isSuccess(true)
                .message("dxf 파일 업로드 성공")
                .build();
    }

    // 뷰어에서 dxf 파일 삭제
    public ResultResponse deleteDxf(String estKey) {

        Estimate estimate = estimateRepository.findByEstKey(estKey).orElseThrow(() -> new CustomLogicException(ExceptionCode.ESTIMATE_NOT_FOUND));
        Long fileKey = estimate.getFileId();
        Files files = filesRepository.findById(fileKey).orElseThrow(() -> new CustomLogicException(ExceptionCode.ESTIMATE_NOT_FOUND));

        try {
            String dxfAddress = files.getS3DxfAddress();

            if (dxfAddress == null) {
                return ResultResponse.of(ResultCode.SUCCESS);
            }

            amazonS3.deleteObject(bucketName, dxfAddress);
            files.deleteDxfByUser();
            filesRepository.save(files);

            return ResultResponse.of(ResultCode.SUCCESS);
        } catch (Exception e) {
            log.error("S3 DXF 삭제 중 에러 발생", e);
            return ResultResponse.of(ResultCode.FAILED);
        }

    }

    // stp 다운로드
    public String downloadSTP(String s3Url) {
        // 멤버키/metal/stp/파일이름
        return getDownloadURL(s3Url);
    }

    // 견적 별 메모 저장 (삭제도)
    public void saveMemo(EstimateDTO.MemoPut memoPut) {
        Optional<Estimate> estimate = estimateRepository.findByEstKey(memoPut.getEstKey());
        if(estimate.isEmpty()){
            throw new CustomLogicException(ExceptionCode.ESTIMATE_NOT_FOUND);
        }

        Estimate estimateEntity = estimate.get();
        estimateEntity.updateMemo(memoPut.getMemo());
        estimateRepository.save(estimateEntity);
    }


    // 견적의 폴더이동 (여러개)
    @Transactional
    public ResultResponse moveEstimateFolder(EstimateDTO.MoveFolder move) {
        Folder folder = folderRepository.findByFolderKey(move.getFolderKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.FOLDER_NOT_FOUND));
        int updateCnt = estimateRepository.moveEstimateFolder(move.getFolderKey(), move.getEstKeys());
        entityManager.clear();

        return ResultResponse.of(ResultCode.SUCCESS, updateCnt);
    }

    // 견적 삭제 (여러개)
    @Transactional
    public ResultResponse deleteEstimate(EstimateDTO.Delete delete) {

        estimateRepository.deleteEstimates(delete.getEstKeys());

        return ResultResponse.of(ResultCode.SUCCESS);
    }

    // 견적 요청 업데이트 (flag : false -> true)
    public ResultResponse updateEstimateRequestFlag(String estKey, String id) {
        Estimate estimate = estimateRepository.findByEstKey(estKey)
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ESTIMATE_NOT_FOUND));
        String errorDetailJson = estimate.getErrorJson();
        JsonNode root = null;
        try {
            root = objectMapper.readTree(errorDetailJson);
        } catch (JsonProcessingException e) {
            throw new CustomLogicException(ExceptionCode.ESTIMATE_ERROR_JSON);
        }

        ArrayNode arrayNode;
        if (root.isArray()) {
            arrayNode = (ArrayNode) root;
        } else {
            throw new CustomLogicException(ExceptionCode.ESTIMATE_ERROR_JSON);
        }

        boolean found = false;
        for (JsonNode error : arrayNode) {
            String errorType = error.path("id").asText();
            if (!errorType.equals(id)) continue;

            JsonNode data = error.path("data");
            ObjectNode errorObject = (ObjectNode) data;

            boolean currentFlag = data.path("flag").asBoolean(false);
            if (currentFlag) {
                throw new CustomLogicException(ExceptionCode.FLAG_ALREADY_TRUE);
            }

            errorObject.put("flag", true);
            found = true;
            break;
        }

        if (!found) {
            throw new CustomLogicException(ExceptionCode.TYPE_NOT_FOUND);
        }

        try {
            String updateJson = objectMapper.writeValueAsString(arrayNode);
            estimate.updateErrorJson(updateJson);
            estimateRepository.save(estimate);
        }catch (JsonProcessingException e) {
            throw new CustomLogicException(ExceptionCode.ESTIMATE_ERROR_JSON);
        }


        return ResultResponse.of(ResultCode.SUCCESS);
    }

    // 파일다운로드 url
    public String getDownloadURL(String address)  {
        try{
            Date expiration = new Date();
            long expTime = expiration.getTime();
            expTime += TimeUnit.MINUTES.toMillis(3);
            expiration.setTime(expTime); // 3분

            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, address)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
            URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

            return url.toString();
        }catch (Exception e){
            log.info(e.getMessage());
            return e.getMessage();
        }
    }

    // todo: 이전 정책버전 견적 재 검증 처리
    public List<EstimateDTO.EstimateValidStatus> reValidEstimates (List<String> estKeys) {
        List<EstimateDTO.EstimateValidStatus> result = new ArrayList<>();
        List<Estimate> estimateList = estimateRepository.findAllByEstKeyIn(estKeys);

        Set<String> foundKeys = estimateList.stream()
                .map(Estimate::getEstKey)
                .collect(Collectors.toSet());

        for (String estKey : estKeys) {
            if (!foundKeys.contains(estKey)) {
                result.add(
                        EstimateDTO.EstimateValidStatus.builder()
                                .estKey(estKey)
                                .status(EstimateStatus.ESTIMATE_FILE_NOT_FOUND)
                                .message("해당 모델링을 찾을 수 없습니다. \n장바구니에서 삭제해주세요.")
                                .build()
                );
            }
        }

        for (Estimate estimate: estimateList) {
            if (estimate.getPolicyVersion().equals(privateValue.getPolicyVersion())) {
                result.add(
                        EstimateDTO.EstimateValidStatus.builder()
                                .estKey(estimate.getEstKey())
                                .status(EstimateStatus.SUCCESS)
                                .message("")
                                .build()
                );
            } else {
                // 기존 옵션으로 다시 검사해야댐
                if (estimate.getMethod().equals("sheet_metal")) { // 판금
                    reValidMetalOption(estimate, result);
                } else if (estimate.getMethod().equals("cnc")) { // 절삭
                    reValidCNCOption(estimate, result);
                }
            }
        }

        return result;
    }
    
    // 판금 재검증
    private void reValidMetalOption(Estimate estimate, List<EstimateDTO.EstimateValidStatus> result) {
        String metaJson = estimateRepository.findMetaJsonByEstkey(estimate.getEstKey());
        double thickness = estimate.getThickness(); // 두께
        String material = estimate.getMaterial(); // 재질
        String surface = estimate.getSurface(); // 표면처리
        String holeJson = estimate.getHoleJson(); // 홀 정보
        boolean isFastShipment = estimate.isFastShipment(); // 단납기 여부
        List<Estimate.ErrorDetail> errorDetailList = new ArrayList<>();

        JsonNode root = null;
        try {
            root = objectMapper.readTree(metaJson);
        } catch (JsonProcessingException e) {
            result.add(
                    EstimateDTO.EstimateValidStatus.builder()
                            .estKey(estimate.getEstKey())
                            .status(EstimateStatus.ESTIMATE_FILE_NOT_FOUND)
                            .message("해당 모델링을 찾을 수 없습니다. \n장바구니에서 삭제 후 다시 업로드 해주세요.")
                            .build()
            );
        }

        JsonNode partsArray = root.get("parts");


        if (!MetalMaterialByThickness.getMaterialByThickness(material, thickness)) {
            result.add(
                    EstimateDTO.EstimateValidStatus.builder()
                            .estKey(estimate.getEstKey())
                            .status(EstimateStatus.ESTIMATE_POLICY_VERSION_MISMATCH)
                            .message("선택하신 재질 '" + material + "'은(는) " + thickness + "mm 두께에서 사용할 수 없습니다.")
                            .build()
            );
            return;
        }

        // 한계치 체크용 대분류 재질
        String majorMaterial = switch (material) {
            case "SPCC", "SPHC", "SS400", "SECC" -> "steel";
            case "SUS304_No1", "SUS304_2B", "SUS304_SH", "SUS304_SP", "SUS304_DP" -> "stain";
            case "AL5052" -> "al";
            default -> material;
        };

        // 리프팅 홀 확인하기
        // 없음, 전해연마 이 외 홀 검사 필요
        if(!surface.equals("NA") && !surface.equals("EPP")){
            log.info("도장 도금 홀 검사 필요");
            String msg = MetalLimit.findLiftHole(partsArray, majorMaterial);
            if(msg != null){
                result.add(
                        EstimateDTO.EstimateValidStatus.builder()
                                .estKey(estimate.getEstKey())
                                .status(EstimateStatus.ESTIMATE_POLICY_VERSION_MISMATCH)
                                .message(msg)
                                .build()
                );
                return;
            }
        }

        // 가격 산정 DTO
        CostDTO.MetalCostDTO costDTO = CostDTO.MetalCostDTO.builder()
                .material(material)
                .thickness(thickness)
                .kg(0.0)
                .totalLength(0.0)
                .bendCost(0)
                .rBendCost(0)
                .surface(surface)
                .metalType("")
                .isFastShipment(isFastShipment)
                .holeJson(holeJson)
                .build();

        // 한계치 찾기
        String errorJson = MetalLimit.findLimitError(partsArray, thickness, material, majorMaterial, errorDetailList, costDTO);
        if (errorJson != null) {
            result.add(
                    EstimateDTO.EstimateValidStatus.builder()
                            .estKey(estimate.getEstKey())
                            .status(EstimateStatus.ESTIMATE_POLICY_VERSION_MISMATCH)
                            .message("가공 정책이 변경되어 옵션 수정이 필요합니다.\n해당 견적(모델링) 의 옵션 수정 후 다시 장바구니에 담아주세요")
                            .build()
            );

            return;
        }

        // 납기일 확인
        OptionDTO.ShipmentDayDTO shipmentDayDTO = Shipment.getShipmentDayByMetal(material, surface);
        Integer day = isFastShipment ? shipmentDayDTO.getExpressDay() : shipmentDayDTO.getStandardDay();
        LocalDate shipmentDate = Shipment.getShipmentDateByMetal(day);
        double totalCost = MetalPrice.getPriceCalc(costDTO);
        int price = setPrice(estimate, isFastShipment, totalCost); // 가격 저장
        estimateRepository.save(estimate);

        result.add(EstimateDTO.EstimateValidStatus.builder()
                .estKey(estimate.getEstKey())
                .status(EstimateStatus.CHANGE_AMOUNT)
                .message("변경된 정책에 따라 견적(모델링)금액이 변경되었습니다. \n 금액을 확인해 주세요.")
                .data(Map.of(
                        "shipmentDate", shipmentDate.toString(),
                        "price", price
                ))
                .build());
    }
    
    // 절삭 재검증
    private void reValidCNCOption(Estimate estimate, List<EstimateDTO.EstimateValidStatus> result) {
        String metaJson = estimateRepository.findMetaJsonByEstkey(estimate.getEstKey());
        String material = estimate.getMaterial(); // 재질
        String surface = estimate.getSurface(); // 표면처리
        String holeJson = estimate.getHoleJson(); // 홀 정보
        boolean isFastShipment = estimate.isFastShipment(); // 단납기 여부
        List<Estimate.ErrorDetail> errorDetailList = new ArrayList<>();

        // 공차 옵션 or 표면거칠기 옵션 선택 시 dxf 필수 체크
        if (estimate.getCommonDiff().equals(CommonDiff.D2.getValue()) || estimate.getRoughness().equals(Roughness.R1.getValue())) {
            Optional<Files> files = filesRepository.findById(estimate.getFileId());

            if (files.isEmpty()) {
                result.add(EstimateDTO.EstimateValidStatus.builder()
                        .estKey(estimate.getEstKey())
                        .status(EstimateStatus.ESTIMATE_FILE_NOT_FOUND)
                        .message("해당 모델링을 찾을 수 없습니다. \n장바구니에서 삭제 후 다시 업로드 해주세요.")
                        .build());
                return;
            }

            if (files.get().getS3DxfAddress() == null) {
                result.add(EstimateDTO.EstimateValidStatus.builder()
                        .estKey(estimate.getEstKey())
                        .status(EstimateStatus.INVALID_ERROR)
                        .message("해당 공차옵션과 표면거칠기 옵션은 dxf 파일이 필수입니다. \ndxf 파일을 업로드해주세요.")
                        .build());
                return;
            }
        }

        // 절삭 분석 결과
        CNCAxisAnalyzer.AnalysisResult analysisResult = cncAxisAnalyzer.analyzeJsonData(metaJson);
        CNCAxisAnalyzer.Result finalResult =  CNCAxisAnalyzer.printResults(analysisResult);

        JsonNode root = null;
        try {
            root = objectMapper.readTree(metaJson);
        }catch (JsonProcessingException e) {
            result.add(EstimateDTO.EstimateValidStatus.builder()
                    .estKey(estimate.getEstKey())
                    .status(EstimateStatus.ESTIMATE_FILE_NOT_FOUND)
                    .message("해당 모델링을 찾을 수 없습니다. \n장바구니에서 삭제 후 다시 업로드 해주세요.")
                    .build());
            return;
        }

        JsonNode parts = root.get("parts");
        JsonNode body = parts.get(0).get("bodies").get(0);

        Double kg = CNCLimit.extractKg(body, material);
        if (kg == null) {
            result.add(EstimateDTO.EstimateValidStatus.builder()
                    .estKey(estimate.getEstKey())
                    .status(EstimateStatus.INVALID_ERROR)
                    .message("허용되지 않는 재질입니다.")
                    .build());
            return;
        }

        // 리프팅 홀 확인하기
        // 없음, 전해연마 이 외 홀 검사 필요
        if(!surface.equals("NA") && !surface.equals("EPP")){
            log.info("도장 도금 홀 검사 필요");
            String msg = CNCLimit.findLiftHole(parts, material);
            if(msg != null){
                result.add(
                        EstimateDTO.EstimateValidStatus.builder()
                                .estKey(estimate.getEstKey())
                                .status(EstimateStatus.ESTIMATE_POLICY_VERSION_MISMATCH)
                                .message(msg)
                                .build()
                );
                return;
            }
        }
        EstimateDTO.CnCOptionPut optionPut = EstimateDTO.CnCOptionPut.builder()
                .estKey(estimate.getEstKey())
                .material(material)
                .surface(surface)
                .commonDiff(estimate.getCommonDiff())
                .roughness(estimate.getRoughness())
                .holeJson(holeJson)
                .isFastShipment(isFastShipment)
                .kg(kg)
                .build();

        String limitError = CNCLimit.findLimitError(parts, estimate.getType(), optionPut, errorDetailList);

        if (limitError != null) {
            result.add(
                    EstimateDTO.EstimateValidStatus.builder()
                            .estKey(estimate.getEstKey())
                            .status(EstimateStatus.ESTIMATE_POLICY_VERSION_MISMATCH)
                            .message("가공 정책이 변경되어 옵션 수정이 필요합니다.\n해당 견적(모델링) 의 옵션 수정 후 다시 장바구니에 담아주세요")
                            .build()
            );

            return;
        }

        CostDTO.CnCCostDTO costDTO = CostDTO.CnCCostDTO.builder()
                .bodyJson(body)
                .material(optionPut.getMaterial())
                .cncType(estimate.getType())
                .kg(kg)
                .surface(optionPut.getSurface())
                .commonDiff(optionPut.getCommonDiff())
                .roughness(optionPut.getRoughness())
                .isFastShipment(optionPut.isFastShipment())
                .build();

        double totalCost = CNCPrice.getPriceByCnc(costDTO, finalResult, logService); // 납기일 제외 총 가격 // fixme: 테스트용 로그
        int price = setPrice(estimate, isFastShipment, totalCost); // 가격 저장
        estimateRepository.save(estimate);

        // 납기일 확인
        OptionDTO.ShipmentDayDTO shipmentDayDTO = Shipment.getShipmentDayByCNC(material, surface, estimate.getType());
        Integer day = isFastShipment ? shipmentDayDTO.getExpressDay() : shipmentDayDTO.getStandardDay();
        LocalDate shipmentDate = Shipment.getShipmentDateByCNC(day);

        result.add(EstimateDTO.EstimateValidStatus.builder()
                .estKey(estimate.getEstKey())
                .status(EstimateStatus.CHANGE_AMOUNT)
                .message("변경된 정책에 따라 견적(모델링)금액이 변경되었습니다. \n 금액을 확인해 주세요.")
                .data(Map.of(
                        "shipmentDate", shipmentDate.toString(),
                        "price", price
                ))
                .build());
    }
}
