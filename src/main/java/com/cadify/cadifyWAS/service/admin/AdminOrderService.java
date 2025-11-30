package com.cadify.cadifyWAS.service.admin;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.dto.admin.order.*;
import com.cadify.cadifyWAS.model.entity.order.OrderItem;
import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import com.cadify.cadifyWAS.model.entity.order.Orders;
import com.cadify.cadifyWAS.repository.AddressRepository;
import com.cadify.cadifyWAS.repository.OrderItemRepository;
import com.cadify.cadifyWAS.repository.OrderRepository;
import com.cadify.cadifyWAS.repository.admin.order.AdminOrderQueryRepository;
import com.cadify.cadifyWAS.repository.admin.order.OrderColumn;
import com.cadify.cadifyWAS.repository.admin.orderItem.AdminOrderItemQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class AdminOrderService {

    // 관리자용 order Repository
    private final AdminOrderQueryRepository adminOrderRepository;
    // 관리자용 orderItem Repository
    private final AdminOrderItemQueryRepository adminOrderItemRepository;
    // 기본 order Repository
    private final OrderRepository orderRepository;
    // 기본 orderItem Repository
    private final OrderItemRepository orderItemRepository;
    // 배송지 repository
    private final AddressRepository addressRepository;

    // 관리자 페이지 Order 조회
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(AdminOrderDTO.OrderRequest request, OrderColumn column){
        return adminOrderRepository.getOrders(request, column);
    }

    // 관리자 페이지 Order Item 조회
    @Transactional(readOnly = true)
    public OrderItemResponse findOrderItemsByOrderKey(AdminOrderDTO.OrderItemRequest request){
        log.info(request.getKey());
        OrderDetails orderDetails = adminOrderRepository.getOrderDetails(request.getKey());
        List<OrderItemRes> orderItem = adminOrderItemRepository.findOrderItemsByOrderKey(request);

        AdminOrderDTO.OrderDetailRes detail = new AdminOrderDTO.OrderDetailRes(
                orderDetails.getOrderKey(), orderDetails.getName(), orderDetails.getFactoryName(), orderDetails.getShipmentDate()
        );
        AdminOrderDTO.ShippingAddressRes address = new AdminOrderDTO.ShippingAddressRes(
                (orderDetails.getAddress() + ", " + orderDetails.getAddressDetail()),
                orderDetails.getDeliveryRequest()
        );

        return new OrderItemResponse(detail, address, orderItem);
    }

    // 날짜별 주문 : (달력) 날짜별 주문 수
    @Transactional(readOnly = true)
    public List<CalendarOrderCountsResponse> getDateOrderCounts(List<String> yearMonth){
        return adminOrderRepository.findDateOrderCounts(yearMonth);
    }
    // 납기일 주문 : 납기일 주문 수
    @Transactional(readOnly = true)
    public List<CalendarOrderCountsResponse> getDeliveryOrderCounts(List<String> yearMonth){
        return adminOrderRepository.findDeliveryOrderCounts(yearMonth);
    }


    // 정산 관리 : 정산 카드 데이터
    @Transactional(readOnly = true)
    public SettlementCardsResponse getSettlementCardsResponse(){
        return adminOrderRepository.getSettlementCardsResponse();
    }

    // Order Item 상태 업데이트
    @Transactional
    public String decideProcessable(AdminOrderDTO.StatusUpdateRequests request){
        OrderItem orderItem = orderItemRepository.findOrderItemByOrderItemKey(request.getKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ORDER_ITEM_NOT_FOUND));
        Orders order = orderRepository.findByOrderKey(orderItem.getOrderKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ORDER_NOT_FOUND));

        switch (request.getStatus()) {
            case REJECTED -> {
                orderItem.updateStatusRejected();
                order.updateOrderStatusRejected();
            }
            case PAID -> {
                orderItem.refuseRejectOrder();
                Set<OrderReceivedStatus> itemStatus = adminOrderItemRepository.findDistinctItemStatus(order.getOrderKey());
                if(!itemStatus.contains(OrderReceivedStatus.REJECT_REQUEST)){
                    order.updateOrderStatusPaid();
                }
            }
            default -> throw new CustomLogicException(ExceptionCode.INVALID_VALUE);
        }
        orderItemRepository.save(orderItem);
        orderRepository.save(order);

        return orderItem.getOrderReceivedStatus().name();
    }

    @Transactional
    public String processDeliveryAndSettlement(AdminOrderDTO.StatusUpdateRequests request){
        Orders order = orderRepository.findByOrderKey(request.getKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ORDER_NOT_FOUND));

        switch (request.getStatus()){
            case SETTLED -> order.updateOrderStatusSettled();
            case DELIVERED -> order.updateOrderStatusDelivered();
            default -> throw new CustomLogicException(ExceptionCode.INVALID_VALUE);
        }

        return order.getOrderReceivedStatus().name();
    }
}


