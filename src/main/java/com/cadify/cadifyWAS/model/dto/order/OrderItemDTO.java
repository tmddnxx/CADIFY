package com.cadify.cadifyWAS.model.dto.order;

import com.cadify.cadifyWAS.model.entity.order.OrderItem;

import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDate;

public class OrderItemDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request{

        private Long estKey;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class Response{

        private String orderItemKey;

        @Column(nullable = false, unique = true)
        private String estName; // 견적이름 (CDF-UUID-UUID숫자-UUID)

        @Builder.Default
        private String method = "sheet_metal"; // 가공방법(기본값 sheet_metal)

        private String estKey;

        private String fileName;

        private int amount; // 수량

        private int unitPrice = 0; // 사용자가 선택한 납기에 대한 총 가격

        private int totalPrice; // 소계 ( 가격 * 수량)

        private int discount; // 할인 금액

        private int paymentPrice; // 결제 금액

        private int itemTotalCost; // 공장용 가격 소계

        private OrderReceivedStatus orderReceivedStatus;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate shipmentDate;

        @JsonProperty("isFastShipment")
        private boolean isFastShipment; // 단납기(true) or 표준납기(false)

        private String material; // 재질

        @Builder.Default
        private String surface = "없음"; // 표면처리

        private String imageAddress;

        private String trackingNumber; // 송장 번호

        private String courier; // 택배사
    }

    @Setter
    @AllArgsConstructor
    @Builder
    public static class AllResponse{

        private String orderItemKey;

        @Column(nullable = false, unique = true)
        private String estName; // 견적이름 (CDF-UUID-UUID숫자-UUID)

        @Builder.Default
        private String method = "sheet_metal"; // 가공방법(기본값 sheet_metal)

        private String estKey;

        private int price; // 견적가격 (개당)

        private int amount; // 수량

        private int itemTotalPrice; // 소계 ( 가격 * 수량)

        private String material; // 재질

        private OrderReceivedStatus status;

        @Builder.Default
        private String surface = "없음"; // 표면처리

    }

}
