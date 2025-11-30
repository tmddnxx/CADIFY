package com.cadify.cadifyWAS.model.dto.admin.order;

import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderItemRes {
    private String itemKey;
    private String fileName;
    private String estKey;
    private String estName;
    private String material;
    private String method;
    private Double thickness;
    private String surface;
    private Integer amount;
    private OrderReceivedStatus status;
    private String rejectedReason;
}
