package com.cadify.cadifyWAS.model.entity.order;

import com.cadify.cadifyWAS.util.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Orders extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderKey;

    private String memberKey;

    private int deliveryCharge;

    private int vat;

    private int totalPrice;

    private int orderDiscount = 0;

    private int orderTotalPaymentPrice = 0;

    private String deliveryMemo;

    private String addressKey;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private OrderReceivedStatus orderReceivedStatus;

    private String managerKey;

    private boolean isPaid = false;

    // 배송(출고) 예정 일
    // TODO: 납기일이 가장 빠른 주문아이템의 납기일 저장
    private LocalDate shipmentDate = LocalDate.now().plusDays(4);

    @Builder
    public Orders(String memberKey, int deliveryCharge, int vat, int totalPrice, int orderDiscount, int orderTotalPaymentPrice, String deliveryMemo, String addressKey, OrderReceivedStatus orderReceivedStatus, String managerKey, boolean isPaid) {
        this.memberKey = memberKey;
        this.deliveryCharge = deliveryCharge;
        this.vat = vat;
        this.totalPrice = totalPrice;
        this.orderDiscount = orderDiscount;
        this.orderTotalPaymentPrice = orderTotalPaymentPrice;
        this.deliveryMemo = deliveryMemo;
        this.addressKey = addressKey;
        this.orderReceivedStatus = orderReceivedStatus;
        this.managerKey = managerKey;
    }

    public void successPaid(List<OrderItem> orderItems) {
        this.isPaid = true;
        this.orderReceivedStatus = OrderReceivedStatus.PAID;
        orderItems.forEach(OrderItem::updateStatusPaid);
    }

    public void updateOrderReceivedStatus(List<OrderItem> items) {
        this.orderReceivedStatus = items.stream()
                .map(OrderItem::getOrderReceivedStatus) // 각 아이템의 상태 꺼냄
                .filter(Objects::nonNull)
                .min(Comparator.comparingInt(OrderReceivedStatus::getPriority)) // 가장 우선순위 높은 상태 선택
                .orElse(OrderReceivedStatus.PAYMENT_PENDING);// 예외: 아이템이 없으면 기본값 PAID
    }

    public void updateOrderStatusSettled(){
        this.orderReceivedStatus = OrderReceivedStatus.SETTLED;
    }
    public void updateOrderStatusDelivered(){
        this.orderReceivedStatus = OrderReceivedStatus.DELIVERED;
    }
    public void updateOrderStatusRejected() { this.orderReceivedStatus = OrderReceivedStatus.REJECTED; }
    public void updateOrderStatusPaid() { this.orderReceivedStatus = OrderReceivedStatus.PAID; }

    public void completePayment(List<OrderItem> orderItems) {
        this.orderReceivedStatus = OrderReceivedStatus.PAID;
        for (OrderItem orderItem : orderItems) {
            orderItem.completePayment();
        }
    }

    public void cancelPayment(List<OrderItem> orderItems) {
        this.orderReceivedStatus = OrderReceivedStatus.CANCELLED;
    }

    public void updateStatusToPaymentPending(List<OrderItem> orderItems) {
        this.orderReceivedStatus = OrderReceivedStatus.PAYMENT_PENDING;
        this.isPaid = false;
        for (OrderItem orderItem : orderItems) {
            orderItem.paymentFailed();
        }
    }

    public void updateStatusToCanceled(List<OrderItem> orderItems) {
        this.orderReceivedStatus = OrderReceivedStatus.CANCELLED;
        this.isPaid = false;

        for (OrderItem orderItem : orderItems) {
            orderItem.cancelPayment();
        }
    }

    public void updateShipmentDate(LocalDate minLocalDate) {
        this.shipmentDate = minLocalDate;
    }


}
