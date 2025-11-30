package com.cadify.cadifyWAS.model.entity.order;


public enum OrderReceivedStatus{

        PAYMENT_PENDING(0), //결제대기
        PAID(1),         // 결제완료
        CANCELLED(2),    // 취소
        REJECT_REQUEST(3),  // 제작불가 심사중
        REJECTED(4),     // 제작불가 (공장: 거절 요청 승인)
        CREATING(5),     // 제작중
        SHIPPING(6),     // 배송중
        DELIVERED(7),    // 배송완료
        SETTLED(8);      // 정산완료

    private final int priority;

    OrderReceivedStatus(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
