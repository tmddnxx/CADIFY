package com.cadify.cadifyWAS.model.dto.order;


import com.cadify.cadifyWAS.model.dto.payment.PaymentDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class OrdersDTO {

    // 주문 생성
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request{

        @Nullable
        private List<Long> cartItemKey;

        private String deliveryMemo;

        @Nullable
        private String addressKey;

        private String managerKey;
    }

    //주문 전체 조회
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class AllResponse {

        //주문 정보
        private String orderKey;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime orderAt;
        private int totalPrice;
        private boolean paid;
        private List<OrderItemDTO.Response> orderItems;

    }

    // 주문 단건 조회
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class Response{
        private String orderKey;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime orderAt;
        private int totalPrice;
        private String address;
        private String addressDetail;
        private String deliveryMemo;
        private String repName;
        private String repPhoneNumber;
        private int vat;
        private int deliveryCharge;
        private boolean paid;
        private List<OrderItemDTO.Response> orderItems;
        private PaymentDTO.OrderResponse nicePayment;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class CreateResponse{
        private String orderKey;
    }

    @Getter
    @Builder
    public static class SuccessResponse{
        private String orderKey;
        private LocalDateTime orderAt;
        private List<OrderItemDTO.Response> orderItems;
        private PaymentDTO.SuccessResponse nicePayment;
        private String address;
        private String addressDetail;
    }
}
