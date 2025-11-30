package com.cadify.cadifyWAS.service.file;

import com.amazonaws.AmazonClientException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.dto.files.GarbageFilesDTO;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.cadify.cadifyWAS.model.entity.Files.Files;
import com.cadify.cadifyWAS.repository.Files.EstimateRepository;
import com.cadify.cadifyWAS.repository.Files.FilesRepository;
import com.cadify.cadifyWAS.repository.factory.estimate.KFactorRepository;
import com.cadify.cadifyWAS.util.PrivateValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log4j2
@RequiredArgsConstructor
public class FilesByFactoryService {

    private final AmazonS3 amazonS3;
    @Value("${aws.s3.bucket.name}")
    private String bucketName;
    private final FilesRepository filesRepository;
    private final EstimateRepository estimateRepository;
    private final GarbageFileService garbageFileService;
    private final SdkService sdkService;
    private final PrivateValue privateValue;
    private final KFactorRepository kFactorRepository;

    // step 파일 다운로드
    public String downloadStepFile(String estKey) {

        // 사용자가 올린 DXF 파일 주소 찾기
        String stepAddress = estimateRepository.findStepAddressByEstKey(estKey);
        if (stepAddress == null) {
            throw new CustomLogicException(ExceptionCode.FILES_NOT_FOUNT);
        }
        return getDownloadURL(stepAddress);
    }

    // Dxf 파일 다운로드 (사용자가 올린 한 모델링에 대해서 1. 사용자가 직접올린 전체형상 dxf)
    public String downloadDxfByUser(String estKey) {
        String dxfAddress = estimateRepository.findUserDxfAddressByEstKey(estKey);

        if(dxfAddress == null){
            return null;
        }

        return getDownloadURL(dxfAddress);
    }

    // 공장이 kFactor 적용해서 새롭게 DXF 파일 뽑아내고 DB, S3에 저장하고 다운로드 할 수있도록 S3 URL 반환
    @Transactional // 판금만 적용댐
    public String downloadDxfByFactory(String estKey) {
        // 0. 전달받은 kFactor가 적용된 dxf 파일 있는지 확인 ( 없거나 변경되면 sdk run, 있거나 변경되지 않았다면 바로 s3 url 반환 ) v
        // 1. estKey에 해당하는 모델링의 step 파일을 s3에서 서버로 다운로드 v
        // 2. 해당 step 파일로 sdk run => dxf 파일 생성완료 v
        // 2-1. sdk에 KFactor 적용 (사용자 업로드 sdk와 kFactor가 있냐 없냐 차이로 구분 가능 ? => compose command 수정으로 가능함) v
        // 3. dxf파일 s3에 저장 및 db에 저장 => memberKey/dxf/factory/fileName_timestamp_0.7(kfactor).dxf
        // 4. 서버경로에서 파일 삭제
        // 5. s3 url 반환

        Estimate estimate = estimateRepository.findByEstKey(estKey).orElseThrow(() -> new CustomLogicException(ExceptionCode.FILES_NOT_FOUNT));
        Files files = filesRepository.findById(estimate.getFileId()).orElseThrow(() -> new CustomLogicException(ExceptionCode.FILES_NOT_FOUNT));
        String existFileName = files.getFileName();
        String memberKey = files.getMemberKey();

        if(estimate.getMethod().equals("cnc")) throw new RuntimeException("절삭은 k팩터를 적용할 수 없습니다.");

        String material = estimate.getMaterial();
        double thickness = estimate.getThickness();
        Double kFactor = kFactorRepository.findKFactorByMaterialAndThickness(material, thickness).getKFactor();

        if (kFactor == null) {
            throw new CustomLogicException(ExceptionCode.COMBINATION_NOT_FOUND);
        }

        /* 0번 */
        // 파일이름에서 kFactor 추출
        String existFactoryAddress = files.getFactoryDxfAddress();
        String existKFactor = null;

        String decodeDxfUrl = null;
        int index = 0;
        String existS3Key = null;

        if (existFactoryAddress != null) { // 주소가 null이 아닐 때만 처리
            // 기존 파일 s3 경로 추출
            decodeDxfUrl = URLDecoder.decode(existFactoryAddress, StandardCharsets.UTF_8);
            index = decodeDxfUrl.indexOf("/metal/dxf/factory/");
            existS3Key = (index != -1) ? memberKey + decodeDxfUrl.substring(index) : null;

            Pattern pattern = Pattern.compile("_([0-9.]+)\\.dxf$");
            Matcher matcher = pattern.matcher(existFactoryAddress);
            if (matcher.find()) {
                existKFactor = matcher.group(1); // 마지막 숫자 추출
                log.info("existKFactor : {}", existKFactor);
            }
        }



        // 비교 전에 null 체크
        if (existFactoryAddress != null && existKFactor != null && existKFactor.equals(kFactor.toString())) {
            log.info("동일한 k팩터 파일 있음");

            return getDownloadURL(existS3Key);
        }

        /* 1번 */
        // 서버로 step 파일 다운로드
        String s3Key = files.getS3StepAddress();
        String basePath = privateValue.getServerRootDir();
        Path stepDownloadPath = null;
        try {
            // 저장할 파일 경로 설정
            stepDownloadPath = Paths.get(basePath, "metal", "stepData", memberKey, existFileName.replaceAll(" ", ""));

            // 필요한 디렉터리 생성 (존재하지 않으면 자동 생성)
            java.nio.file.Files.createDirectories(stepDownloadPath.getParent());

            // S3에서 파일 가져오기
            S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, s3Key));

            // 파일 저장
            try (InputStream inputStream = s3Object.getObjectContent();
                 FileOutputStream outputStream = new FileOutputStream(stepDownloadPath.toFile())) {

                byte[] buffer = new byte[8192]; // 8KB 버퍼 (기본값보다 조금 더 큰 크기로 최적화)
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        /* 2번 */
        // sdk run => /jsonOut/memberKey/filname_timestamp.dxf 생성
        String dxfPath = sdkService.executeDockerByFactory(memberKey, existFileName, kFactor); // dxf 생성 경로
        String uploadFileName = existFileName.endsWith(".stp") ? existFileName.substring(0, existFileName.lastIndexOf(".stp")) : existFileName;
        String s3PathKey = memberKey + "/metal/dxf/factory/" + uploadFileName + "_" + kFactor + ".dxf"; // S3에 저장할 경로
        
        /* 3번 */
        // S3 및 DB 저장
        try{
            amazonS3.putObject(bucketName, s3PathKey, new File(dxfPath)); // S3에 업로드
            String dxfFactoryAddress = amazonS3.getUrl(bucketName, s3PathKey).toString(); // S3 주소 가져오기
            files.uploadDxfByFactory(dxfFactoryAddress); // DB에 저장

            try{
                // S3에서 기존에 있는 다른 kFactor 파일 삭제
                if(existS3Key != null){
                    amazonS3.deleteObject(bucketName, existS3Key);
                }
            }catch (Exception e){ // 기존 파일 삭제 실패시
                GarbageFilesDTO exist = new GarbageFilesDTO();
                exist.setPath(existS3Key); // 실패한 step파일 경로 저장
                garbageFileService.saveGarbageFiles(exist); // garbage 테이블에 저장
            }

        }catch (Exception e){ // 실패시 롤백처리
            log.info("DXF 업로드 예외 발생 : {} ",e.getMessage());
            amazonS3.deleteObject(bucketName, s3PathKey); // db 롤백시 s3도 롤백
            // 실패하면 배치돌리기 위해 저장
            GarbageFilesDTO garbageFilesDTO = new GarbageFilesDTO();
            garbageFilesDTO.setPath(s3PathKey); // 실패한 파일 경로 저장
            garbageFileService.saveGarbageFiles(garbageFilesDTO); // garbage 테이블에 저장

            throw new RuntimeException("잠시 후 다시 시도해주세요.");
        }
        
        /* 4번 */
        // 서버에 저장된 step 파일 및 dxf 파일 삭제
        try {
            sdkService.deleteFiles(Paths.get(dxfPath).getParent());
            sdkService.deleteFiles(stepDownloadPath.getParent());
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
            throw new RuntimeException("파일 처리중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }

        /* 5번 */
        // 다운로드 url 반환
        return getDownloadURL(s3PathKey);
    }




    // 사용자가 dxf 파일을 올렸는지 확인
    public boolean findOwnerDxfURL(Long estKey) throws Exception {
        Estimate estimate = estimateRepository.findById(estKey).orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));
        Files files = filesRepository.findById(estimate.getFileId()).orElseThrow(() -> new Exception("파일을 찾을 수 없습니다."));

        return files.getS3DxfAddress() != null;
    }

    // 파일다운로드 url
    private String getDownloadURL(String address) throws AmazonClientException {
        Date expiration = new Date();
        long expTime = expiration.getTime();
        expTime += TimeUnit.MINUTES.toMillis(3);
        expiration.setTime(expTime); // 3분

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, address)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        return url.toString();
    }



}
