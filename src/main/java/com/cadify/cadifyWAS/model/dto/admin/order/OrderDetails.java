package com.cadify.cadifyWAS.model.dto.admin.order;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@Getter
public class OrderDetails {
    // 주문 세부정보
    private String orderKey;
    private String name;
    private String factoryName;
    private String shipmentDate;
    // 배송지 정보
    private String address;
    private String addressDetail;
    private String deliveryRequest;

    public OrderDetails(String orderKey, String name, String factoryName, LocalDate shipmentDate, String address, String addressDetail, String deliveryRequest){
        this.orderKey = orderKey;
        this.name = name;
        this.factoryName = factoryName;
        this.shipmentDate = shipmentDate.format(DateTimeFormatter.ISO_DATE);
        this.address = address;
        this.addressDetail = addressDetail;
        this.deliveryRequest = deliveryRequest;
    }
}
