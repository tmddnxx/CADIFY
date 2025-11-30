package com.cadify.cadifyWAS.model.dto.files;

import com.cadify.cadifyWAS.service.file.common.CommentType;
import com.cadify.cadifyWAS.service.file.common.EstimateStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public class EstimateDTO {


    @Getter
    @Builder
    @AllArgsConstructor
    public static class Post{
        private String memberKey; // 사용자 pk
        private String estKey; // 견적 퍼블릭 키 (UUID)
        @Setter
        private Long fileId; // 파일 pk
        private String fileName; // 파일 원본이름
        private String folderKey; // 폴더 퍼블릭 키
        private String estName; // 견적이름 (CDF-UUID-UUID숫자-UUID)
        private String method; // 가공방법
        private String type; // 가공타입
        private int price; // 견적가격
        private int cost; // 원가
        private int otherPrice; // 사용자가 선택하지 않은 납기에 대한 총 가격
        private String holeJson; // 홀 정보 json
        @JsonProperty("isFastShipment")
        private boolean isFastShipment; // 단납기(true) or 표준납기(false)
        private String material; // 재질
        private double thickness; // 두께
        private double kg; // 형상 무게 
        private String surface; // 표면처리
        private String coatingColor; // 도장 색상 (표면처리 도장선택시)
        @JsonProperty("isChamfer")
        private boolean isChamfer; // 실면취
        private LocalDateTime createdAt; // 생성날짜
        private String errorCode; // 견적 가능 or 불가 (sdk)
        private String erorrDetails; // 한계치 오류 내용
        private long fileSize; // 파일 사이즈
        private String bbox; // 형상 bbox (수치)
        private LocalDate policyVersion; // 정책 버전 날짜
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MetalOptionPut{
        @NotNull
        private String estKey; // 견적 퍼블릭 키 (UUID)
        @NotNull
        @JsonProperty("isFastShipment")
        private boolean isFastShipment; // 단납기(true) or 표준납기(false)
        @NotNull
        private String material; // 재질
        @NotNull
        private String surface; // 표면처리
        @NotNull
        @JsonProperty("isChamfer")
        private boolean isChamfer; // 실면취

        private String holeJson; // 홀탭 json

        private String coatingColor; // 도장 색상 (표면처리 도장 선택시)
        /* 서버에서 측정 */
        private double kg; // 형상 무게
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class CnCOptionPut{
        @NotNull
        private String estKey; // 견적 퍼블릭 키 (UUID)
        @NotNull(message = "납기일은 필수사항입니다")
        @JsonProperty("isFastShipment")
        private boolean isFastShipment; // 단납기(true) or 표준납기(false)
        @NotNull(message = "재질은 필수사항입니다")
        private String material; // 재질
        @NotNull(message = "표면처리는 필수사항입니다")
        private String surface; // 표면처리
        @NotNull
        @JsonProperty("isChamfer")
        private boolean isChamfer; // 실면취
        @NotNull(message = "공차옵션은 필수사항입니다")
        private String commonDiff; // 공차 (D0: ISO 2768 일반(기본), D1: +-0.10mm, D2: +-0.05mm)
        @NotNull(message = "표면거칠기옵션은 필수사항입니다")
        private String roughness; // 표면거칠기 (R0: Ra3.2(기본), R1: Ra1.6)

        private String holeJson; // 홀탭 json
        private String coatingColor; // 도장 색상 (표면처리 도장 선택시)
        /* 서버에서 측정 */
        private double kg; // 형상 무게
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class Response{
        private String estKey; // 견적 pk
        private String memberKey; // 사용자 pk
        private String folderKey; // 폴더 키
        private String fileName; // 파일 이름
        private String estName; // 견적이름 (CDF-UUID-UUID숫자-UUID)
        private String method; // 가공방법
        private String type; // 가공타입
        private int price; // 견적가격
        private int otherPrice; // 사용자가 선택하지 않은 납기에 대한 총 가격
        private int cost; // 원가
        private String holeJson; // 홀 정보 json
        @JsonProperty("isFastShipment")
        private boolean isFastShipment; // 단납기(true) or 표준납기(false)
        private String material; // 재질
        private double thickness; // 두께
        private double kg; // 형상 무게
        private String surface; // 표면처리
        private String coatingColor; // 도장 색상 (표면처리 도장선택시)
        @JsonProperty("isChamfer")
        private boolean isChamfer; // 실면취
        private String commonDiff; // 공차
        private String roughness; // 표면거칠기
        private String memo; // 메모
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt; // 생성날짜
        private List<Integer> errorCode; // 견적 가능 or 불가
        private String errorDetails; // 한계치 오류내용
        private String stepS3; // step파일 s3 주소
        private String imageUrl; // 이미지 s3 주소
        private String dxfName; // 사용자가 올린 dxf 이름
        private String tempKey; // 임시키
        private long fileSize; // 파일 사이즈

        private OptionDTO.BBox bbox; // 형상 bbox (수치)
        private Integer standardDay; // 표준 납기일 수
        private Integer expressDay; // 단납기 납기일 수
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ListResponse {
        private List<Response> response;
        private long totalFileSize; // 총 파일 사이즈
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusResponse{
        private String estKey; // 견적 pk
        private String fileName; // 파일이름
        private boolean isSuccess; // 성공실패 플래그
        private String message; // 메시지
        private Object data;
    }

    @Getter
    public static class Request{
        private String stpUrl;
    }

    @Getter
    public static class MemoPut{
        private String estKey; // 견적 pk
        private String memo; // 메모
    }

    @Getter
    public static class MoveFolder{
        private List<String> estKeys; // 견적 pk
        private String folderKey; // 폴더 pk
    }

    @Getter
    public static class Delete{
        private List<String> estKeys; // 견적 pk
    }

    @Getter
    @AllArgsConstructor
    public static class S3Address {
        private String s3StepAddress; // step 파일 s3 주소
        private String s3DxfAddress; // dxf 파일 s3 주소
        private String factoryDxfAddress; // 공장용 dxf 파일 s3 주소
        private String imageAddress; // 이미지 파일 s3 주소
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ErrorFlag {
        private boolean flag;
        private CommentType comment;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EstimateValidStatus {
        private String estKey;
        private EstimateStatus status;
        private String message;
        private Object data;
    }
}
