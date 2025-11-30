package com.cadify.cadifyWAS.model.dto.admin.estimate;

import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class AdminEstimateDTO {
    // todo: 이곳에 관리자 견적 관련 DTO를 정의합니다.
    @Getter
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class EstimateResponse {
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
        private String dxfName; // 사용자가 올린 dxf 이름
        private long fileSize; // 파일 사이즈
        private OptionDTO.BBox bbox; // 형상 bbox (수치)
        @Setter
        private String stepS3; // step파일 s3 주소
        @Setter
        private String imageUrl; // 이미지 s3 주소
    }
}
