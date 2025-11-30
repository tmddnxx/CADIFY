package com.cadify.cadifyWAS.service.file.common;

import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class FileCommon {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 리스트를 문자열로 변환
    public static String convertListToString(List<?> list){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // List<ErrorDetail>을 JSON 문자열로 변환
            if (list.isEmpty()) return null;

            List<?> distinctList = list.stream()
                    .distinct()
                    .collect(Collectors.toList());

            return objectMapper.writeValueAsString(distinctList);
        } catch (Exception e) {
            throw new RuntimeException("Error converting ErrorDetail list to string", e);
        }
    }

    // 문자열을 숫자 리스트로 변환
    public static List<Integer> convertStringToNumberList(String input) {
        if (input == null) return null;

        try {
            return objectMapper.readValue(input, new TypeReference<List<Integer>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error converting string to number list: {}", e.getMessage());
            throw new RuntimeException("Error converting string to number list", e);
        }
    }

    // 문자열을 리스트로 변환
    public static List<Estimate.ErrorDetail> convertStringToErrorList(String input) {
        if (input == null) return null;

        try {
            return objectMapper.readValue(input, new TypeReference<List<Estimate.ErrorDetail>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error converting string to list: {}", e.getMessage());
            throw new RuntimeException("Error converting string to list", e);
        }
    }

    // 숫자 사이 공백 및 . 제거
    public static String replaceSpaceAndDot(String input) {
        return input
                .replaceAll("(?<=\\d)[.](?=\\d)", "") // 숫자 사이 . 제거
                .replaceAll("\\s+", "")               // 공백 제거
                .toLowerCase();
    }

    // 파일 검증 (파일명, 확장자, 특수문자 등)
    public static void validateFile(String originFileName, MultipartFile file){
        if (file.isEmpty()) {
            throw new RuntimeException("파일을 업로드해주세요.");
        }

        if (!originFileName.toLowerCase().endsWith(".step")) {
            throw new RuntimeException(".step 형식의 확장자만 업로드 가능합니다.");
        }

        String normalizedFileName = Normalizer.normalize(
                originFileName.substring(0, originFileName.toLowerCase().lastIndexOf(".step")),
                Normalizer.Form.NFC
        );

        if (!normalizedFileName.matches("^[a-zA-Z0-9ㄱ-ㅎ가-힣 _,-]+$")) {
            throw new RuntimeException("파일이름에 특수문자는 사용 불가합니다.");
        }
    }

    // 파일이름 + 타임스탬프 변환
    public static String formatFileName(String fileName, LocalDateTime now){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String formattedNow = now.format(formatter);

        String fileOriginName = fileName.replaceAll("(?i)\\.step$", "");

        String result = fileOriginName + "_info_" + formattedNow;

        return result;
    }

    // 네이밍 규칙
    public static String generatedEstName(String type){
        // 첫 번째 UUID 생성 (하이픈 제거)
        String uuid1 = UUID.randomUUID().toString().replace("-", "");

        // 첫 번째 4글자
        String part1 = uuid1.substring(0, 4);

        // 네 자리 숫자
        int randomNum = (int) (Math.random() * 10000); // 0 ~ 9999 사이의 숫자

        // 마지막 4글자
        String part2 = uuid1.substring(4, 8);
        String base = type.equals("sheet_metal") ? "CDFS-" : "CDFC-";
        // "CDF-4개-숫자네개-4개" 형식으로 반환
        return base + part1 + "-" + String.format("%04d", randomNum) + "-" + part2;
    }

    public static String getColoredSTPName(String name) {
        String fileName = name.split(".stp")[0];

        return fileName + "_0-1-1-1_1_colored.stp";
    }

    // 타입검사
    public static boolean isSameMethod(String type, MethodType methodType) {
        try {
            return MethodType.valueOf(type) == methodType;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // dxf 이름 추출
    /**
     * dxfAddress 에서 파일명만 추출하고,
     * (.*)_info_.*\.(.*) 패턴에 맞으면 "baseName.extension" 형태로 변환,
     * 아니면 원래 파일명 반환
     *
     * @param dxfAddress 전체 경로 혹은 URL
     * @return 변환된 파일명 또는 원본 파일명, null if input is null
     */
    public static String extractDxfFileName(String dxfAddress) {
        if (dxfAddress == null) {
            return null;
        }

        // 경로에서 파일명만 추출
        String fileNameWithExt = dxfAddress.substring(dxfAddress.lastIndexOf("/") + 1);

        Pattern pattern = Pattern.compile("^(.*)_info_.*\\.(\\w+)$");
        Matcher matcher = pattern.matcher(fileNameWithExt);

        if (matcher.matches()) {
            String baseName = matcher.group(1);
            String extension = matcher.group(2);
            return baseName + "." + extension;
        } else {
            return fileNameWithExt;
        }
    }
}
