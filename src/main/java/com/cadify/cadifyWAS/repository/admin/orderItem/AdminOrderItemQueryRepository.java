package com.cadify.cadifyWAS.repository.admin.orderItem;

import com.cadify.cadifyWAS.model.dto.admin.dashboard.PreferredProcessingTypeResponse;
import com.cadify.cadifyWAS.model.dto.admin.order.AdminOrderDTO;
import com.cadify.cadifyWAS.model.dto.admin.order.OrderItemRes;
import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;

import java.util.List;
import java.util.Set;

public interface AdminOrderItemQueryRepository {

    List<OrderItemRes> findOrderItemsByOrderKey(AdminOrderDTO.OrderItemRequest request);

    PreferredProcessingTypeResponse findPreferredProcessingType();

    Set<OrderReceivedStatus> findDistinctItemStatus(String orderKey);
}
