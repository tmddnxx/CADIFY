package com.cadify.cadifyWAS.model.dto.cart;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

public class CartItemDTO {

    @Getter
    @Setter
    @Builder
    public static class GetCartResponse{

        private Long cartItemKey;

        private String estKey;

        private String fileName; // 파일원본이름

        private String s3StepAddress; // step 파일의 s3 주소

        private String method; // 가공방법(기본값 sheet_metal)

        private int amount; // 수량

        private int unitPrice; // 개당 금액

        private int totalPrice; // 총 금액

        private int discount; // 할인 금액

        private int paymentPrice; // 최종 결제 금액

        @JsonProperty("isFastShipment")
        private boolean isFastShipment; // 단납기(true) or 표준납기(false)

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate shipmentDate;

        private String material; // 재질

        private String surface; // 표면처리

        @JsonProperty("isChamfer")
        private boolean isChamfer; // 실면취

        private String holeJson; // 홀탭 구분 json 파일

        private String imageAddress;

        private Integer standardShipmentDay; // 표준 납기일수

        private Integer expressShipmentDay; // 단납기 납기일수
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateAmount{
        private Long cartItemKey;
        private int amount;
    }
}
