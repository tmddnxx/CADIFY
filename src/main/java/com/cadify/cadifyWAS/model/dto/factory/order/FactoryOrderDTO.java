package com.cadify.cadifyWAS.model.dto.factory.order;

import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class FactoryOrderDTO {

    @Getter
    @NoArgsConstructor
    public static class OrderItemRequest {
        private String orderKey;
        private List<OrderReceivedStatus> status;
    }

    @Getter
    @NoArgsConstructor
    public static class RejectRequest {
        private String orderKey;
        private String itemKey;
        private String rejectReasonCategory;
        private String rejectReasonDetail;
    }

    @Getter
    @NoArgsConstructor
    public static class StartShippingRequest {
        private String courier;
        private String trackingNumber;
        private String orderKey;
        private List<String> itemKeyList;
    }

    @Getter
    @NoArgsConstructor
    public static class ConfirmRequest {
        private String orderKey;
        private String itemKey;
    }
}


//    @Getter
//    @Setter
//    @NoArgsConstructor
//    public static class TrackingRequest {
//        private String trackingNumber;
//        private String courier;
//    }
//
//    @Getter
//    @Builder
//    public static class TrackingResponse {
//        private String orderKey;
//        private String orderItemKey;
//        private String trackingNumber;
//        private String courier;
//        private OrderReceivedStatus status;
//    }

//@Getter
//@Builder
//@AllArgsConstructor
//public static class BaseResponse {
//    private String orderKey;
//    private String customerName;
//    private LocalDateTime createdAt;
//    private int totalCost;
//    private OrderReceivedStatus overallOrderReceivedStatus;
//}
//
//@Getter
//@Builder
//@AllArgsConstructor
//public static class DetailResponse {
//    private BaseResponse baseInfo;
//    private List<OrderItemDTO.Response> orderItems;
//    private MemberDTO.MemberInfo customerInfo;
//}