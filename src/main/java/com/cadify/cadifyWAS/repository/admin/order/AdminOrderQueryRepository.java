package com.cadify.cadifyWAS.repository.admin.order;

import com.cadify.cadifyWAS.model.dto.admin.dashboard.OrderCardResponse;
import com.cadify.cadifyWAS.model.dto.admin.order.*;
import com.querydsl.core.Tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminOrderQueryRepository {

//  ------------------------  admin/(공용)

    // Admin 페이지 공용 주문리스트 조회 템플릿
    List<OrderResponse> getOrders(AdminOrderDTO.OrderRequest request, OrderColumn orderColumn);

    // Admin 페이지 주문 세부사항 조회 ( 주문 세부 정보 + 배송지 )
    OrderDetails getOrderDetails(String orderKey);

//  ------------------------  admin/dashboard

    // Admin Dashboard : 대시보드 주문 카드 데이터 조회
    OrderCardResponse findOrderCardStatus(LocalDate today);

    // Admin Dashboard : 대시보드 매출 카드 데이터 조회
    Tuple findRevenueCardStatus(LocalDateTime today, LocalDateTime thisWeek, LocalDateTime thisMonth);

    // Admin Dashboard : 월 별 매출 차트
    List<Tuple> findMonthlyRevenueChartStatus(LocalDateTime twelveMonthAgo);

//  ------------------------  admin/order

    // Admin Order : 총 주문 : 주문 상세 정보 조회
    AdminOrderDTO.OrderDetailRes findOrderDetails(String orderKey);

    // Admin Order : 정산 관리 : 정산 데이터 카드 조회
    SettlementCardsResponse getSettlementCardsResponse();

//  ------------------------  admin/date

    // Admin Date : 날짜별 주문 수 조회
    List<CalendarOrderCountsResponse> findDateOrderCounts(List<String> yearMonth);

//  ------------------------  admin/shipment

    // Admin Shipment : 납기일 별 주문 수 조회
    List<CalendarOrderCountsResponse> findDeliveryOrderCounts(List<String> yearMonth);

}
