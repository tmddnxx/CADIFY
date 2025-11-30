package com.cadify.cadifyWAS.model.dto.factory.estimate;

import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class FactoryEstimateDTO {
    // todo: 이곳에 공장 견적 관련 DTO를 정의합니다.
    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class EstimateResponse {
        private String estKey; // 견적 pk
        private String fileName; // 파일 이름
        private String estName; // 견적이름 (CDF-UUID-UUID숫자-UUID)
        private String method; // 가공방법
        private String type; // 가공타입
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
        private String stepS3; // step파일 s3 주소
        private String imageUrl; // 이미지 s3 주소
        private String factoryDxfAddress; // 공장 dxf 주소
        private String s3DxfAddress; // s3 dxf 주소
        private String dxfName; // 사용자가 올린 dxf 이름

        private OptionDTO.BBox bbox; // 형상 bbox (수치)
    }
}
