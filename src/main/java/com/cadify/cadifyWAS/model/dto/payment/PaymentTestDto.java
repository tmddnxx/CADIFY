package com.cadify.cadifyWAS.model.dto.payment;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PaymentTestDto {

    @NoArgsConstructor
    @Getter
    public static class ConfirmResponse{
        private String resultCode;
        private String resultMsg;
        private String tid;
    }

    @Data
    public class CancelResponse {
        private String resultCode;
        private String resultMsg;
        private String tid;
        private String cancelledTid;
        private String cancelAmount; // 취소 금액
    }

    // 결제 조회 응답 DTO 예시
    @Data
    public class TransactionQueryResponse {
        private String resultCode;
        private String resultMsg;
        private String tid;
        private String orderId;
        private int amount;
        private String status; // 'paid', 'cancelled' 등
    }
}
