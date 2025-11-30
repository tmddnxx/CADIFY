package com.cadify.cadifyWAS.model.dto.admin.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Getter
@NoArgsConstructor
public class SettlementOrdersResponse {
    private String orderKey;
    private String name;
    private String company;
    private String orderDate;
    private String depositDate;
    private Integer price;
    private String settlementDueDate;
    private String status;
    private Long leftDays;

    public SettlementOrdersResponse(String orderKey, String name, String company, LocalDateTime orderDate, Integer price){
        this.orderKey = orderKey;
        this.name = name;
        this. company = company;
        this. orderDate = orderDate.format(DateTimeFormatter.ISO_DATE);
        this. depositDate = "---";
        this.price = price;
        this. settlementDueDate = orderDate.plusDays(5).format(DateTimeFormatter.ISO_DATE);
        this.status = "WAITING";
        this.leftDays = ChronoUnit.DAYS.between(LocalDate.now(), orderDate.toLocalDate().plusDays(5));
    }
}
