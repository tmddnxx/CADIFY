package com.cadify.cadifyWAS.model.dto.admin.order;

import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdminOrderDTO {

    // 총 주문 : 조회
    @NoArgsConstructor
    @Data
    public static class OrderRequest {
        private String material = "";
        private String method = "";
        private LocalDate dateStart = null;
        private LocalDate dateEnd = null;
        private List<OrderReceivedStatus> status = new ArrayList<>();
        private String search = "";
        private String dateBy = "";
        private String sortDirection = "DESC";
    }

    // 주문 상세 내역 조회
    @Getter
    @NoArgsConstructor
    public static class OrderItemRequest{
        private String key;
        private OrderReceivedStatus status;
    }

    // 총 주문 : 주문 상세 정보 템플릿
    @Getter
    public static class OrderDetailRes {
        private final String orderKey;
        private final String name;
        private final String factoryName;
        private final String shipmentDate;

        public OrderDetailRes(String orderKey, String memberName, String factoryName, String shipmentDate){
            this.orderKey = orderKey;
            this.name = memberName;
            this.factoryName = factoryName;
            this.shipmentDate = shipmentDate;
        }
    }

    // 총 주문 : 배송지 정보 템플릿
    @Getter
    public static class ShippingAddressRes {
        private final String address;
        private final String deliveryRequest;

        public ShippingAddressRes(String address, String deliveryRequest){
            this.address = address;
            this.deliveryRequest = deliveryRequest;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class StatusUpdateRequests{
        private String key;
        private OrderReceivedStatus status;
    }
}
