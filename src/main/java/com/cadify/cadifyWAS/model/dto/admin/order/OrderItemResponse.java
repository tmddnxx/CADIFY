package com.cadify.cadifyWAS.model.dto.admin.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class OrderItemResponse {
    private AdminOrderDTO.OrderDetailRes orderDetail;
    private AdminOrderDTO.ShippingAddressRes shippingAddress;
    private List<OrderItemRes> orderItems;
}
