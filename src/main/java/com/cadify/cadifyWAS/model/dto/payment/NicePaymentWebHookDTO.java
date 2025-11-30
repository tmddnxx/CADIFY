package com.cadify.cadifyWAS.model.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NicePaymentWebHookDTO {

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {
        private String resultCode;      // 결제 결과 코드
        private String resultMsg;       // 결제 결과 메시지
        private String tid;             // 결제 승인 키
        private String orderId;         // 상점 거래 고유번호 → Payment.orderKey
        private String status;          // 결제 상태 (paid, cancelled 등)
        private String paidAt;          // 결제 완료 시점 (ISO 8601)
        private String failedAt;        // 결제 실패 시점 (ISO 8601 or "0")
        private String cancelledAt;     // 결제 취소 시점 (ISO 8601 or "0")
        private String payMethod;       // 결제 수단
        private Integer amount;         // 결제 금액
        private Integer balanceAmt;     // 취소 가능 금액
        private String goodsName;       // 상품명
        private String receiptUrl;      // 매출 전표 URL
    }
}
