package com.cadify.cadifyWAS.model.dto.payment;


import com.cadify.cadifyWAS.model.entity.payment.Payment;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class PaymentDTO {

    @Getter
    @Builder
    public static class SuccessResponse{
        //결제 방법
        private String method;
        //총 결제 금액
        private int amount;
    }

    @Getter
    @NoArgsConstructor
    public static class Confirm{

       private String authResultCode;
       private String authResultMsg;
       private String tid;
       private String clientId;
       private String orderId;
       private String amount;
       private String authToken;
       private String signature;
       private List<String> estKeys;
    }

    @Builder
    @Getter
    public static class OrderResponse{
        private String orderId;
        private String paymentKey;
        private int amount;
        private String orderKey;
        private String approvedAt;
        private String requestedAt;
        private String status;
        private String method;

        public static OrderResponse paymentToOrder(Payment payment) {
            return OrderResponse.builder()
                    .orderId(payment.getOrderKey())
                    .paymentKey(payment.getPaymentKey())
                    .amount(payment.getAmount()) // null이면 0 할당
                    .orderKey(payment.getOrderKey())
                    .status(payment.getStatus())
                    .method(payment.getPayMethod())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class CancelRequest{
        private String cancelReason;
    }

    @Getter
    @NoArgsConstructor
    public static class CancelRequestByNetwork{
        private String orderId;
    }


    @Getter
    @Builder
    public static class ReceiptResponse{
        private String receiptUrl;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {

        private String resultCode;
        private String resultMsg;
        private String tid;
        private String cancelledTid;    // 🔹 취소 TID
        private String orderId;
        private String ediDate;
        private String signature;       // 🔹 위변조 검증용 SHA256
        private String status;          // [paid, ready, failed, cancelled, ...]
        private String paidAt;
        private String failedAt;
        private String cancelledAt;
        private String payMethod;       // [card, vbank, bank, ...]
        private Integer amount;
        private Integer balanceAmt;
        private String goodsName;
        private String mallReserved;
        private Boolean useEscrow;
        private String currency;
        private String channel;         // [pc, mobile]
        private String approveNo;
        private String buyerName;
        private String buyerTel;
        private String buyerEmail;
        private Boolean issuedCashReceipt;
        private String receiptUrl;
        private String mallUserId;

        private Coupon coupon;
        private Card card;

        @Getter
        @Setter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Coupon {
            private int couponAmt;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Card {
            private String cardCode;
            private String cardName;
            private String cardNum;
            private int cardQuota;
            private boolean interestFree;
            private String cardType;
            private boolean canPartCancel;
            private String acquCardCode;
            private String acquCardName;
        }
    }

}
