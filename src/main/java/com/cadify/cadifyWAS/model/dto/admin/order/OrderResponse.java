package com.cadify.cadifyWAS.model.dto.admin.order;

import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class OrderResponse {
    private String orderKey;
    private String date;
    private String name;
    private Long details;
    private String shipmentDate;
    private Integer orderPrice;
    private String orderStatus;
    private String modifiedAt;

    public OrderResponse(String orderKey, LocalDateTime date, String name, Long details, LocalDate shipmentDate, Integer orderPrice, OrderReceivedStatus status, LocalDateTime modifiedAt){
        this.orderKey = orderKey;
        this.date = date.toLocalDate().format(DateTimeFormatter.ISO_DATE);
        this.name = name;
        this.details = details;
        this.shipmentDate = shipmentDate.format(DateTimeFormatter.ISO_DATE);
        this.orderPrice = orderPrice;
        this.orderStatus = status.name();
        this.modifiedAt = modifiedAt.toLocalDate().format(DateTimeFormatter.ISO_DATE);
    }

//    @Override
//    public String toString() {
//        return "OrderResponse{" +
//                "date='" + date + '\'' +
//                ", orderKey='" + orderKey + '\'' +
//                ", name='" + name + '\'' +
//                ", details=" + details +
//                ", shipmentDate='" + shipmentDate + '\'' +
//                ", orderPrice=" + orderPrice +
//                ", orderStatus='" + orderStatus + '\'' +
//                '}';
//    }
}