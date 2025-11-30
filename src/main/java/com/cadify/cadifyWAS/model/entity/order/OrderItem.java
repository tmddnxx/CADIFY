package com.cadify.cadifyWAS.model.entity.order;

import com.cadify.cadifyWAS.util.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderItemKey;

    private String orderKey;

    private String memberKey;

    /**
     * 견적 관련
     */
    private String estKey; // 견적 uuid

    private String estName; // 견적이름 (CDF-UUID-UUID숫자-UUID)

    private String method; // 가공방법(판금 sheet_metal) or 절삭 cnc

    private String type; // 가공타입(절곡, 절단, 레이저, 밀링 등)

    @Builder.Default
    private int price = 0; // 사용자가 선택한 납기에 대한 총 가격

    @Builder.Default
    private int cost = 0; // 원가

    private boolean isFastShipment; // 단납기(true) or 표준납기(false)

    private String material; // 재질

    @Builder.Default
    private double thickness = 0; // 두께

    @Builder.Default
    private String surface = "없음"; // 표면처리

    private String coatingColor; // 도장 색상 (표면처리 도장선택시)

    @Builder.Default
    private boolean isChamfer = false; // 실면취

    @Builder.Default
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

    @Column(length = 1000)
    private String s3DxfAddress; // 사용자가 올린 dxf 파일의 s3 주소

    @Column( length = 1000)
    private String factoryDxfAddress; // 공장에서 생성한 dxf 파일의 s3 주소
    /* https:// ~ /memberKey/dxf/factory/파일이름_timestamp_kFactor.dxf */

    @Column( length = 1000)
    private String imageAddress; // 이미지 주소

    @Column(nullable = false, columnDefinition = "text")
    private String metaJson; // sdk에서 얻어온 파일 json정보

    // 공장용
    private String trackingNumber; // 송장 번호

    private String courier; // 택배사

    private String rejectReasonCategory; // 거절 사유 카테고리

    private String rejectReasonDetail;  // 거절 사유 설명

    @Version
    private Long version;


    //공장에서 받았는지 거절했는지 boolean?
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderReceivedStatus orderReceivedStatus = OrderReceivedStatus.PAYMENT_PENDING;

    // OrderItem 결제 금액 관련
    private int amount; // 수량

    @Builder.Default
    private int unitPrice = 0; // 사용자가 선택한 납기에 대한 총 가격

    private int totalPrice; // 소계 ( 가격 * 수량)

    private int discount; // 할인 금액

    private int paymentPrice; // 결제 금액

    private LocalDate shipmentDate; // 배송일

    public OrderItem(String cj, String errorTracking) {
        this.estName = cj;
        this.trackingNumber = errorTracking;
    }

    /**
     * 승인 / 제작 중 / 배송 중 / 거절
     */

    public void rejectOrder(String category, String detail) {
        this.orderReceivedStatus = OrderReceivedStatus.REJECTED;
        this.rejectReasonCategory = category;
        this.rejectReasonDetail = detail;
    }

    public void refuseRejectOrder(){
        this.orderReceivedStatus = OrderReceivedStatus.PAID;
        this.rejectReasonCategory = null;
        this.rejectReasonDetail = null;
    }

    public void updateTrackingInfo(String trackingNumber, String courier) {
        this.trackingNumber = trackingNumber;
        this.courier = courier;
        this.orderReceivedStatus = OrderReceivedStatus.SHIPPING;
    }

    public void completePayment() {
        this.orderReceivedStatus = OrderReceivedStatus.PAID;
    }

    public void cancelPayment() {
        this.orderReceivedStatus = OrderReceivedStatus.CANCELLED;
    }

    public void paymentFailed() {
       this.orderReceivedStatus = OrderReceivedStatus.PAYMENT_PENDING;
    }

    // 결제 상태로 업데이트
    public void updateStatusPaid() {
        this.orderReceivedStatus = OrderReceivedStatus.PAID;
    }

    // 제작중 상태로 업데이트
    public void updateStatusCreating(){
        this.orderReceivedStatus = OrderReceivedStatus.CREATING;
    }


    // 거절 상태로 업데이트
    public void rejectProcess(String rejectReasonCategory, String rejectReasonDetail){
        this.orderReceivedStatus = OrderReceivedStatus.REJECTED;
        this.rejectReasonCategory = rejectReasonCategory;
        this.rejectReasonDetail = rejectReasonDetail;
    }


    // 결제 취소 상태로 업데이트
    public void updateStatusCanceled(){
        this.orderReceivedStatus = OrderReceivedStatus.CANCELLED;
    }

    // 제작 불가 승인
    public void updateStatusRejected(){
        this.orderReceivedStatus = OrderReceivedStatus.REJECTED;
    }

    // 배송중 상태로 업데이트
    public void updateStatusShipping(){
        this.orderReceivedStatus = OrderReceivedStatus.SHIPPING;
    }

    // 배송 완료 상태로 업데이트
    public void updateStatusDelivered(){
        //현재 상태가 배송완료보다 우선 순위가 낮을때만 변경
        if(this.orderReceivedStatus.getPriority() < 5){
            this.orderReceivedStatus = OrderReceivedStatus.DELIVERED;
        }
    }



}
