package com.cadify.cadifyWAS.model.dto.admin.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class DeliveryOrderResponse {
    private String date;
    private Integer orderCount;
    private List<OrderResponse> orders;
}
