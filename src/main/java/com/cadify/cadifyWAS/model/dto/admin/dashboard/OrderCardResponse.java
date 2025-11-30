package com.cadify.cadifyWAS.model.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCardResponse {
    private Long totalOrders;
    private Long newOrders;
    private Long todayDeliver;
    private Long delayedDeliver;
}
