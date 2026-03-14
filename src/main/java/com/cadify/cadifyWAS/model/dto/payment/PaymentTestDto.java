package com.cadify.cadifyWAS.model.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PaymentTestDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConfirmResponse{
        private String resultCode;
        private String resultMsg;
        private String tid;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CancelResponse {
        private String resultCode;
        private String resultMsg;
        private String tid;
        private String cancelledTid;
        private String cancelAmount; // 취소 금액
    }

    // 결제 조회 응답 DTO 예시
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransactionQueryResponse {
        private String resultCode;
        private String resultMsg;
        private String tid;
        private String orderId;
        private int amount;
        private String status; // 'paid', 'cancelled' 등
    }
}
