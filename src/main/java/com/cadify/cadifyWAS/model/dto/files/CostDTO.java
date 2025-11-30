package com.cadify.cadifyWAS.model.dto.files;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;


public class CostDTO {

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class MetalCostDTO{
        private String material; // 재질
        private Double kg; // 무게
        private Double totalLength; // 가공 길이
        private Double thickness; // 두께
        private int bendCost; // 일반절곡 비용
        private int bendCnt; // 일반절곡 개수
        private int rBendCost; // R 절곡 비용
        private int rBendCnt; // R 절곡 개수
        private String surface; // 표면처리
        private String metalType; // 형상 타입 ( Flat, Folded)
        private boolean isFastShipment; // 단납기(true) or 표준납기(false)
        private String holeJson; // 구멍 json
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class CnCCostDTO{
        private JsonNode bodyJson; // 메타 json의 body
        private String material; // 재질
        private Double kg; // 무게
        private String commonDiff; // 공차 (D0: ISO 2768 일반(기본), D1: +-0.10mm, D2: +-0.05mm)
        private String roughness; // 표면거칠기 (R0: Ra3.2(기본), R1: Ra1.6)
        private String surface; // 표면처리
        private String cncType; // 가공타입 (lathe, milling, lathe_milling, pure_lathe)
        private boolean isFastShipment; // 단납기(true) or 표준납기(false)
        private String holeJson; // 구멍 json
    }

}
