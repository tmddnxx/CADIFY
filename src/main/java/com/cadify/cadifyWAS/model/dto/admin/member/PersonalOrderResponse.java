package com.cadify.cadifyWAS.model.dto.admin.member;

import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@Getter
public class PersonalOrderResponse {
    private String date;
    private String orderKey;
    private String shipmentDate;
    private Integer orderPrice;
    private String orderStatus;

    public PersonalOrderResponse(LocalDateTime date, String orderKey, LocalDate shipmentDate, Integer orderPrice, OrderReceivedStatus status){
        this.date = date.toLocalDate().format(DateTimeFormatter.ISO_DATE);
        this.orderKey = orderKey;
        this.shipmentDate = shipmentDate.format(DateTimeFormatter.ISO_DATE);
        this.orderPrice = orderPrice;
        this.orderStatus = status.name();
    }
}
