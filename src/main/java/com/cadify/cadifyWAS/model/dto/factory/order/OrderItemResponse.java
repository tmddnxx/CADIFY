package com.cadify.cadifyWAS.model.dto.factory.order;

import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@Getter
public class OrderItemResponse {
    private String itemKey; // 아이템 PK
    private String fileName;    // 파일 이름
    private String estName; // 견적 이름
    private String material;    // 재질
    private String method;  // 가공유형
    private Double thickness;   // 두께
    private Integer amount; // 수량
    private String shipmentDate;    // 납기일
    private String surface; // 표면처리
    private String estKey;  // 견적 키
    private String imageUrl;    // 아이템 형상 이미지
    private String status;  // 아이템 제작 상태
    private String orderKey;    // 주문 pk
    private String trackingNumber;  // 송장번호
    private String courier; // 배송업체

    public OrderItemResponse(
            String itemKey, String fileName, String estName, String material, String method, Double thickness,
            Integer amount, LocalDate shipmentDate, String surface, String estKey, String imageUrl, OrderReceivedStatus status, String orderKey, String trackingNumber, String courier)
    {
        this.itemKey = itemKey;
        this.fileName = fileName;
        this.estName = estName;
        this.material = material;
        this.method = method;
        this.thickness = thickness;
        this.amount = amount;
        this.shipmentDate = shipmentDate.format(DateTimeFormatter.ISO_DATE);
        this.surface = surface;
        this.estKey = estKey;
        this.imageUrl = imageUrl;
        this.status = status.name();
        this.orderKey = orderKey;
        this.trackingNumber = trackingNumber;
        this.courier = courier;
    }
}
