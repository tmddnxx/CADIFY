package com.cadify.cadifyWAS.repository.factory.order;

import com.cadify.cadifyWAS.model.dto.admin.order.CalendarOrderCountsResponse;
import com.cadify.cadifyWAS.model.dto.admin.order.SettlementCardsResponse;
import com.cadify.cadifyWAS.model.dto.factory.dashboard.DashboardCardResponse;
import com.cadify.cadifyWAS.model.dto.factory.dashboard.WeeklyOrderCountResponse;
import com.cadify.cadifyWAS.model.dto.factory.order.FactoryOrderDTO;
import com.cadify.cadifyWAS.model.dto.factory.order.OrderItemResponse;
import com.cadify.cadifyWAS.model.dto.factory.order.OrderResponse;
import com.cadify.cadifyWAS.model.dto.factory.order.RejectedItemResponse;
import com.cadify.cadifyWAS.model.entity.factory.FactoryType;
import com.cadify.cadifyWAS.model.entity.order.OrderItem;
import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import com.querydsl.core.Tuple;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface FactoryOrderQueryRepository {

    // Factory Dashboard : 대시보드 카드 데이터
    DashboardCardResponse getDashboardCardData(String factoryKey, FactoryType factoryType);
    // Factory Dashboard : 대시보드 월별 매출 차트 데이터
    List<Tuple> getMonthlyRevenueData(String factoryKey, FactoryType factoryType, LocalDateTime twelveMonthAgo);
    // Factory Dashboard : 이번주 요일별 주문 수 데이터
    List<Tuple> getWeeklyOrderCount(String factoryKey, FactoryType factoryType, LocalDateTime sevenDaysAgo);

    // Factory order : order 상태에 따른 order list 조회
    List<OrderResponse> getOrderList(String factoryKey, FactoryType factoryType, List<OrderReceivedStatus> status);
    // Factory order : 주문에 포함된 order item 조회 (상태값 nullable = true)
    List<OrderItemResponse> getOrderItemLists(String factoryKey, FactoryType factoryType, String orderKey, List<OrderReceivedStatus> status);
    // Factory order : 주문 아이템 제작 가능 판정
    List<OrderItemResponse> confirmOrderItem(String factoryKey, FactoryType factoryType, FactoryOrderDTO.ConfirmRequest request);
    // Factory order : 주문 아이템 제작 불가 판정
    RejectedItemResponse rejectOrderItem(String factoryKey, FactoryType factoryType, FactoryOrderDTO.RejectRequest request);
    // Factory order : 주문 아이템 리스트 송장 등록
    List<OrderItemResponse> startShippingAndReturnRemainedItems(String factoryKey, FactoryType factoryType, FactoryOrderDTO.StartShippingRequest request);

    // Factory shipment : 날짜별 납기 주문 수 조회
    List<CalendarOrderCountsResponse> getShipmentDateOrderCount(String factoryKey, FactoryType factoryType, List<String> yearMonth);
    // Factory settlement : 최근 정산 내역 조회
    List<OrderResponse> getRecentlySettlementOrders(String factoryKey, FactoryType factoryType);
    // Factory settlement : 정산 카드 데이터 조회
    SettlementCardsResponse getSettlementCardsData(String factoryKey, FactoryType factoryType);

    // Factory order : 주문 상태 업데이트
    void updateOrderStatus(String orderKey, OrderReceivedStatus status);
}
