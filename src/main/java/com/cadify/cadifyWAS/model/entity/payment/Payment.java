package com.cadify.cadifyWAS.model.entity.payment;

import com.cadify.cadifyWAS.model.dto.payment.NicePaymentWebHookDTO;
import com.cadify.cadifyWAS.util.base.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String paymentKey;

    private String resultCode;

    private String resultMsg;

    private String tid;

    @Column(unique = true)
    private String orderKey;

    private String status;

    private String paidAt;

    private String failedAt;

    private String cancelledAt;

    private String payMethod;

    private int amount;

    private int balanceAmt;

    private String goodsName;

    private String receiptUrl;

    public void updateByWebHook(NicePaymentWebHookDTO.Request webhookDTO) {
        this.resultCode = webhookDTO.getResultCode();
        this.resultMsg = webhookDTO.getResultMsg();
        this.tid = webhookDTO.getTid();
        this.orderKey = webhookDTO.getOrderId(); // orderId → orderKey로 저장
        this.status = webhookDTO.getStatus();
        this.paidAt = webhookDTO.getPaidAt();
        this.failedAt = webhookDTO.getFailedAt();
        this.cancelledAt = webhookDTO.getCancelledAt();
        this.payMethod = webhookDTO.getPayMethod();
        this.amount = webhookDTO.getAmount() != null ? webhookDTO.getAmount() : 0;
        this.balanceAmt = webhookDTO.getBalanceAmt() != null ? webhookDTO.getBalanceAmt() : 0;
        this.goodsName = webhookDTO.getGoodsName();
        this.receiptUrl = webhookDTO.getReceiptUrl();
    }

}
