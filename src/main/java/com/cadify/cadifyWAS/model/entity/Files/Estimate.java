package com.cadify.cadifyWAS.model.entity.Files;

import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.util.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "estimate", indexes = {
        @Index(name = "idx_memberKey", columnList = "memberKey"),
        @Index(name = "idx_deletedAt", columnList = "deletedAt"),
})
public class Estimate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 견적 pk

    @Column(nullable = false, unique = true)
    private String estKey; // 견적 퍼블릭 키 (UUID)

    @Column(nullable = false)
    private String memberKey; // 사용자 pk

    @Column(nullable = false, unique = true)
    private Long fileId; // 파일 pk

    @Column
    @Builder.Default
    private String folderKey = null; // 소속 폴더 pk (0이면 root에 위치해있음)

    @Column(nullable = false)
    private String fileName; // 파일원본이름

    @Column(nullable = false, unique = true)
    private String estName; // 견적이름 (CDF-UUID-UUID숫자-UUID)

    @Column
    private String method; // 가공방법(판금 sheet_metal) or 절삭 cnc

    @Column
    private String type; // 가공타입(절곡, 절단, 레이저, 밀링 등)

    @Column
    @Builder.Default
    private int price = 0; // 사용자가 선택한 납기에 대한 총 가격

    @Column
    @Builder.Default
    private int otherPrice = 0; // 사용자가 선택하지 않은 납기에 대한 총 가격

    @Column
    @Builder.Default
    private int cost = 0; // 원가

    @Column
    private boolean isFastShipment; // 단납기(true) or 표준납기(false)

    @Column
    private String material; // 재질

    @Column
    @Builder.Default
    private double thickness = 0; // 두께

    @Column
    @Builder.Default
    private String surface = "없음"; // 표면처리
    
    @Column
    private String coatingColor; // 도장 색상 (표면처리 도장선택시)
    
    @Column
    @Builder.Default
    private boolean isChamfer = false; // 실면취

    @Column
    @Builder.Default
    private double kg = 0.0; // 형상 무게
    
    @Column
    private String commonDiff; // 공차
    
    @Column
    private String roughness; // 표면거칠기

    @Column(columnDefinition = "text")
    private String memo; // 메모
    
    @Column(columnDefinition = "text")
    private String holeJson; // 홀 정보 json

    @Column(columnDefinition = "text")
    private String errorCode; // warning code 내용 (sdk)

    @Column (columnDefinition = "text")
    private String errorJson; // 한계치 에러 내용 (type, data, message, faceIds)

    @Column
    private LocalDateTime createdAt; // 생성날짜

    @Column(nullable = false)
    private long fileSize; // 파일 사이즈

    @Column(columnDefinition = "text")
    private String bbox; // bounding box 정보

    @Column
    private LocalDate policyVersion; // 정책 버전 날짜

    @Column
    private Integer standardShipmentDay; // 표준 납기일수

    @Column
    private Integer expressShipmentDay; // 단납기 납기일수

    @Column
    private LocalDateTime deletedAt; // 삭제된 날짜 (null이면 삭제되지 않음)

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class ErrorDetail{ // 한계치 오류 저장객체
        @Builder.Default
        private UUID id = UUID.randomUUID();
        private String type;
        private String message;
        private Object data;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ErrorDetail)) return false;
            ErrorDetail that = (ErrorDetail) o;
            return Objects.equals(type, that.type) &&
                    Objects.equals(message, that.message) &&
                    Objects.equals(data, that.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, message, data);
        }
    }

    // 파일 이름 변경
    public void updateFileName(String fileName){
        this.fileName = fileName;
    }
    
    // 판금 옵션 변경
    public void updateMetalOptions(EstimateDTO.MetalOptionPut optionPut){
        updateOptions(optionPut.isFastShipment(), optionPut.getMaterial(), optionPut.getSurface(), optionPut.getCoatingColor(), optionPut.isChamfer(), optionPut.getKg());
    }

    // 절삭 옵션 변경
    public void updateCnCOptions(EstimateDTO.CnCOptionPut optionPut){
        updateOptions(optionPut.isFastShipment(), optionPut.getMaterial(), optionPut.getSurface(), optionPut.getCoatingColor(), optionPut.isChamfer(), optionPut.getKg());
        this.roughness = optionPut.getRoughness();
        this.commonDiff = optionPut.getCommonDiff();
    }
    
    // 공통 옵션 변경
    private void updateOptions(boolean fastShipment, String material, String surface, String coatingColor, boolean chamfer, double kg) {
        this.isFastShipment = fastShipment;
        this.material = material;
        this.surface = surface;
        this.coatingColor = coatingColor;
        this.isChamfer = chamfer;
        this.kg = kg;
    }

    public void updateShipmentDate(Integer standardShipmentDay, Integer expressShipmentDay) {
        this.standardShipmentDay = standardShipmentDay;
        this.expressShipmentDay = expressShipmentDay;
    }

    // 홀JSON 업데이트
    public void updateHoleJson(String holeJson){
        this.holeJson = holeJson;
    }

    // 가격 업데이트
    public void updatePrice(int price, int cost, int otherPrice){
        this.cost = cost;
        this.price = price;
        this.otherPrice = otherPrice;
    }

    // 옵션 변경 후 에러항목 업데이트
    public void updateErrorJson(String errorJson){
        this.errorJson = errorJson;
    }

    // 메모 저장
    public void updateMemo(String memo) {
        this.memo = memo;
    }


}
