package com.cadify.cadifyWAS.repository.admin.order;

import com.cadify.cadifyWAS.model.dto.admin.dashboard.OrderCardResponse;
import com.cadify.cadifyWAS.model.dto.admin.order.*;
import com.cadify.cadifyWAS.model.entity.QAddress;
import com.cadify.cadifyWAS.model.entity.factory.Factory;
import com.cadify.cadifyWAS.model.entity.factory.QFactory;
import com.cadify.cadifyWAS.model.entity.member.QOAuthMember;
import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import com.cadify.cadifyWAS.model.entity.order.QOrderItem;
import com.cadify.cadifyWAS.model.entity.order.QOrders;
import com.cadify.cadifyWAS.repository.util.QueryDslUtils;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class AdminOrderQueryRepositoryImpl implements AdminOrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QOrders order = QOrders.orders;
    private static final QOAuthMember member = QOAuthMember.oAuthMember;
    private static final QOrderItem orderItem = QOrderItem.orderItem;
    private static final QFactory factory = QFactory.factory;
    private static final QAddress address = QAddress.address1;

    public AdminOrderQueryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

// ------------------------ Admin 공용

    // Admin 공용 : Order 리스트 조회 템플릿
    @Override
    public List<OrderResponse> getOrders(AdminOrderDTO.OrderRequest request, OrderColumn column) {
        BooleanExpression dateCondition = null;
        if (request.getDateStart() != null && request.getDateEnd() != null) {
            dateCondition = column.getDateCondition(order, request.getDateStart(), request.getDateEnd());
        }
        OrderSpecifier<?> orderSpecifier = column.getOrderSpecifier(order, request.getSortDirection());

        return queryFactory
                .select(Projections.constructor(OrderResponse.class,
                        order.orderKey, order.createdAt, member.memberName, orderItem.count().coalesce(0L).as("details"),
                        order.shipmentDate, order.totalPrice, order.orderReceivedStatus, order.modifiedAt))
                .from(order)
                .leftJoin(member).on(member.memberKey.eq(order.memberKey))
                .leftJoin(orderItem).on(orderItem.orderKey.eq(order.orderKey))
                .where(
                        // 날짜 범위
                        dateCondition,
                        // 검색어 : orderKey, memberName
                        searchCondition(request.getSearch(), order.orderKey, member.memberName),
                        // orderItem 가공 방법
                        QueryDslUtils.eqString(orderItem.method, request.getMethod()),
                        // orderItem 재질
                        QueryDslUtils.eqString(orderItem.material, request.getMaterial()),
                        // status
                        QueryDslUtils.eqEnumArr(order.orderReceivedStatus, request.getStatus())
                )
                .groupBy(order.orderKey, order.createdAt, member.memberName, order.shipmentDate, order.totalPrice, order.orderReceivedStatus)
                .orderBy(orderSpecifier)
                .fetch();
    }

    // 주문 세부 사항 (배송지, 담당 공장 정보, Order 정보) 조회
    @Override
    public OrderDetails getOrderDetails(String orderKey) {

        String factoryName = extractFactoryName(orderKey);

        return queryFactory.select(Projections.constructor(OrderDetails.class,
                        order.orderKey, member.memberName, Expressions.constant(factoryName), order.shipmentDate,
                        address.address, address.addressDetail, order.deliveryMemo))
                .from(order)
                .innerJoin(member).on(order.memberKey.eq(member.memberKey))
                .innerJoin(address).on(order.addressKey.eq(address.addressKey))
                .where(order.orderKey.eq(orderKey))
                .fetchOne();
    }

// ------------------------ Admin Dashboard

    // Admin Dashboard : 주문관련 카드 데이터 조회
    @Override
    public OrderCardResponse findOrderCardStatus(LocalDate today) {
        NumberExpression<Long> delayedDeliver = new CaseBuilder().when(order.shipmentDate.lt(today)).then(1L).otherwise(0L).sum().coalesce(0L);

        NumberExpression<Long> todayNewOrder = new CaseBuilder()
                .when(order.createdAt.goe(today.atStartOfDay())).then(1L).otherwise(0L).sum().coalesce(0L);

        NumberExpression<Long> todayDeliver = new CaseBuilder()
                .when(order.shipmentDate.eq(today)).then(1L).otherwise(0L).sum().coalesce(0L);

        return queryFactory
                .select(Projections.constructor(OrderCardResponse.class, order.count(), todayNewOrder, todayDeliver, delayedDeliver))
                .from(order)
                .fetchOne();
    }

    // Admin Dashboard : 매출관련 카드 데이터 조회
    @Override
    public Tuple findRevenueCardStatus(LocalDateTime today, LocalDateTime thisWeek, LocalDateTime thisMonth) {
        NumberExpression<Integer> todayRevenue
                = QueryDslUtils.sumRevenueBetween(order.createdAt, order.totalPrice, today, today.plusDays(1));
        NumberExpression<Integer> yesterdayRevenue
                = QueryDslUtils.sumRevenueBetween(order.createdAt, order.totalPrice, today.minusDays(1), today);
        NumberExpression<Integer> thisWeekRevenue
                = QueryDslUtils.sumRevenueBetween(order.createdAt, order.totalPrice, thisWeek, thisWeek.plusWeeks(1));
        NumberExpression<Integer> lastWeekRevenue
                = QueryDslUtils.sumRevenueBetween(order.createdAt, order.totalPrice, thisWeek.minusWeeks(1), thisWeek);
        NumberExpression<Integer> thisMonthRevenue
                = QueryDslUtils.sumRevenueBetween(order.createdAt, order.totalPrice, thisMonth, thisMonth.plusMonths(1));
        NumberExpression<Integer> lastMonthRevenue
                = QueryDslUtils.sumRevenueBetween(order.createdAt, order.totalPrice, thisMonth.minusMonths(1), thisMonth);

        return queryFactory
                .select(
                        todayRevenue, yesterdayRevenue, thisWeekRevenue, lastWeekRevenue, thisMonthRevenue, lastMonthRevenue
                )
                .from(order)
                .fetchOne();
    }

    // Admin Dashboard 월 매출 차트 조회
    @Override
    public List<Tuple> findMonthlyRevenueChartStatus(LocalDateTime twelveMonthAgo) {
        return queryFactory
                .select(
                        order.createdAt.year().as("year"),
                        order.createdAt.month().as("month"),
                        order.totalPrice.sum().coalesce(0).as("revenue")
                ).from(order)
                .where(order.createdAt.goe(twelveMonthAgo))
                .groupBy(order.createdAt.year(), order.createdAt.month())
                .orderBy(order.createdAt.year().desc(), order.createdAt.month().desc())
                .fetch();
    }

// ------------------------ Admin Order

    // Admin Order : 주문 별 주문 상세 정보 조회
    @Override
    public AdminOrderDTO.OrderDetailRes findOrderDetails(String orderKey) {
        return queryFactory
                .select(Projections.constructor(AdminOrderDTO.OrderDetailRes.class,
                        order.orderKey, member.memberName, order.shipmentDate))
                .from(order)
                .leftJoin(member).on(order.memberKey.eq(member.memberKey))
                .where(
                        QueryDslUtils.eqString(order.orderKey, orderKey))
                .fetchOne();
    }

    // Admin Order : settlement : 정산 카드 데이터
    @Override
    public SettlementCardsResponse getSettlementCardsResponse() {

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        NumberExpression<Long> waitCount = new CaseBuilder()
                .when(order.orderReceivedStatus.in(OrderReceivedStatus.DELIVERED, OrderReceivedStatus.SHIPPING))
                .then(1L).otherwise(0L).sum().coalesce(0L);
        NumberExpression<Long> waitAmount = new CaseBuilder()
                .when(order.orderReceivedStatus.in(OrderReceivedStatus.DELIVERED, OrderReceivedStatus.SHIPPING))
                .then(order.totalPrice.longValue()).otherwise(0L).sum().coalesce(0L);

        NumberExpression<Long> completeCount = new CaseBuilder()
                .when(order.orderReceivedStatus.eq(OrderReceivedStatus.SETTLED)
                        .and(order.createdAt.goe(startOfMonth)))
                .then(1L).otherwise(0L).sum().coalesce(0L);
        NumberExpression<Long> completeAmount = new CaseBuilder()
                .when(order.orderReceivedStatus.eq(OrderReceivedStatus.SETTLED)
                        .and(order.createdAt.goe(startOfMonth)))
                .then(order.totalPrice.longValue()).otherwise(0L).sum().coalesce(0L);

        NumberExpression<Long> totalCount = new CaseBuilder()
                .when(order.orderReceivedStatus.eq(OrderReceivedStatus.SETTLED))
                .then(1L).otherwise(0L).sum().coalesce(0L);
        NumberExpression<Long> totalAmount = new CaseBuilder()
                .when(order.orderReceivedStatus.eq(OrderReceivedStatus.SETTLED))
                .then(order.totalPrice.longValue()).otherwise(0L).sum().coalesce(0L);

        return queryFactory
                .select(Projections.constructor(SettlementCardsResponse.class,
                        waitCount, waitAmount, completeCount, completeAmount, totalCount, totalAmount))
                .from(order)
                .fetchOne();
    }

    // Admin Order : date : 날짜별 주문 수 조회
    @Override
    public List<CalendarOrderCountsResponse> findDateOrderCounts(List<String> yearMonth) {

        StringTemplate createdAtDay = Expressions.stringTemplate("TO_CHAR({0}, 'YYYY-MM-DD')", order.createdAt);

        // 월 단위 계산
        YearMonth startYm = YearMonth.parse(yearMonth.get(0));
        YearMonth endYm = YearMonth.parse(yearMonth.get(yearMonth.size() - 1));

        LocalDateTime start = startYm.atDay(1).atStartOfDay();
        LocalDateTime end = endYm.atEndOfMonth().atTime(LocalTime.MAX);

        return queryFactory
                .select(Projections.constructor(CalendarOrderCountsResponse.class,
                        createdAtDay, order.orderKey.count()))
                .from(order)
                .where(order.createdAt.between(start, end))
                .groupBy(createdAtDay)
                .fetch();
    }

    // Admin Order : shipment : 납기일 주문 수 조회
    @Override
    public List<CalendarOrderCountsResponse> findDeliveryOrderCounts(List<String> yearMonth) {

        StringTemplate shipmentDay = Expressions.stringTemplate("TO_CHAR({0}, 'YYYY-MM-DD')", order.shipmentDate);

        // 월 단위 계산
        YearMonth startYm = YearMonth.parse(yearMonth.get(0));
        YearMonth endYm = YearMonth.parse(yearMonth.get(yearMonth.size() - 1));

        LocalDate start = startYm.atDay(1);
        LocalDate end = endYm.atEndOfMonth();

        return queryFactory
                .select(Projections.constructor(CalendarOrderCountsResponse.class,
                        shipmentDay, order.orderKey.count()))
                .from(order)
                .where(
                        order.shipmentDate.between(start, end),
                        order.orderReceivedStatus.in(OrderReceivedStatus.PAID, OrderReceivedStatus.CREATING, OrderReceivedStatus.PAYMENT_PENDING, OrderReceivedStatus.REJECTED))
                .groupBy(shipmentDay)
                .fetch();
    }

// ------------------------ 내부 Util

    // Order 검색어 조건 메서드
    private BooleanExpression searchCondition(String search, StringPath... paths) {
        if (!StringUtils.hasText(search) || paths.length == 0) return null;

        BooleanExpression expression = paths[0].containsIgnoreCase(search);
        for (int i = 1; i < paths.length; i++) {
            expression = expression.or(paths[i].containsIgnoreCase(search));
        }

        return expression;
    }

    // 판금, 절삭에 따른 담당 공장 이름 추출
    private String extractFactoryName(String orderKey) {

        Set<String> methodSet = new HashSet<>(
                queryFactory
                        .select(orderItem.method)
                        .from(orderItem)
                        .where(orderItem.orderKey.eq(orderKey))
                        .fetch()
        ).stream().filter(Objects::nonNull).map(String::toUpperCase).collect(Collectors.toSet());

        boolean isMetal = methodSet.contains("SHEET_METAL");
        boolean isCNC = methodSet.contains("CNC");

        if(isMetal && isCNC){
            return "판금+절삭";
        }else if(isMetal){
            return "판금";
        }else if(isCNC){
            return "절삭";
        }else{
            return "";
        }
    }
}
