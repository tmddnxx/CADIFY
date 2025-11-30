package com.cadify.cadifyWAS.model.entity.cart;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.service.file.enumValues.common.Shipment;
import com.cadify.cadifyWAS.util.base.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class CartItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long cartItemKey;

    @NotNull
    private Long cartKey;

    /**
     * 견적 관련
     */
    private String estKey; // 견적 uuid

    private String estName; // 견적이름 (CDF-UUID-UUID숫자-UUID)

    private String method; // 가공방법(판금 sheet_metal) or 절삭 cnc

    private String type; // 가공타입(절곡, 절단, 레이저, 밀링 등)

    private int cost = 0; // 원가

    private boolean isFastShipment; // 단납기(true) or 표준납기(false)

    private String material; // 재질

    private double thickness = 0; // 두께

    private String surface = "없음"; // 표면처리

    private String coatingColor; // 도장 색상 (표면처리 도장선택시)

    private boolean isChamfer = false; // 실면취

    private double kg = 0.0; // 형상 무게

    private String commonDiff; // 공차

    private String roughness; // 표면거칠기

    @Column(columnDefinition = "text")
    private String memo; // 메모

    @Column(columnDefinition = "text")
    private String holeJson; // 홀 정보 json

    @Column(columnDefinition = "text")
    private String bbox; // bounding box 정보

    private LocalDate policyVersion; // 정책 버전 날짜

    private Integer standardShipmentDay; // 표준 납기일수

    private Integer expressShipmentDay; // 단납기 납기일수

    /**
     * 파일 관련
     */
    @Column(nullable = false)
    private String fileKey; // 파일 pk

    @Column(nullable = false)
    private String fileName; // 파일이름_타임스탬프(S3)

    @Column(nullable = false, length = 1000)
    private String s3StepAddress; // step 파일의 s3 주소

    @Column( length = 1000)
    private String s3DxfAddress; // 사용자가 올린 dxf 파일의 s3 주소

    @Column( length = 1000)
    private String factoryDxfAddress; // 공장에서 생성한 dxf 파일의 s3 주소
    /* https:// ~ /memberKey/dxf/factory/파일이름_timestamp_kFactor.dxf */

    @Column( length = 1000)
    private String imageAddress; // 이미지 주소

    @Column(nullable = false, columnDefinition = "text")
    private String metaJson; // sdk에서 얻어온 파일 json정보

    /**
     * 장바구니 금액 관련
     */
    private int amount = 1; // 수량

    private int unitPrice = 0; // 사용자가 선택한 납기에 대한 총 가격

    private int totalPrice; // 소계 ( 가격 * 수량)

    private int discount; // 할인 금액

    private int paymentPrice; // 결제 금액

    private LocalDate shipmentDate; // 배송일

    public void updateAmount(int amount) {
        this.amount = amount;
        updateTotalPrice(); // 내 totalPrice만 새로 계산
    }

    public void updateTotalPrice() {
        log.info("updateTotalPrice 들어옴");
        this.totalPrice = amount * unitPrice;
        calculatePaymentPrice();
        updatePaymentPrice();
    }

    private void calculatePaymentPrice() {
        double discountRate = Discount.getDiscountRate(this.amount);
        this.discount = (int)(unitPrice * discountRate * amount);
    }

    public void updatePaymentPrice() {
        this.paymentPrice = totalPrice - discount;
        log.info("paymentPrice ={}", this.paymentPrice );
    }

    public void updateShipmentDate() {
        if(this.getMethod().equals("cnc")){
            if(this.isFastShipment()){
                this.shipmentDate =  Shipment.getShipmentDateByCNC(this.expressShipmentDay);
            } else {
                this.shipmentDate =  Shipment.getShipmentDateByCNC(this.standardShipmentDay);
            }
        } else if(this.getMethod().equals("sheet_metal")){
            if(this.isFastShipment()){
                this.shipmentDate =  Shipment.getShipmentDateByMetal(this.expressShipmentDay);
            } else {
                this.shipmentDate =  Shipment.getShipmentDateByMetal(this.standardShipmentDay);
            }
        } else {
            throw new CustomLogicException(ExceptionCode.CALCULATE_SHIPMENT_DATE_FAILED);
        }
    }
}
