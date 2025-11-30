package com.cadify.cadifyWAS.repository.factory.order;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.dto.admin.order.CalendarOrderCountsResponse;
import com.cadify.cadifyWAS.model.dto.admin.order.SettlementCardsResponse;
import com.cadify.cadifyWAS.model.dto.factory.dashboard.DashboardCardResponse;
import com.cadify.cadifyWAS.model.dto.factory.order.FactoryOrderDTO;
import com.cadify.cadifyWAS.model.dto.factory.order.OrderItemResponse;
import com.cadify.cadifyWAS.model.dto.factory.order.OrderResponse;
import com.cadify.cadifyWAS.model.dto.factory.order.RejectedItemResponse;
import com.cadify.cadifyWAS.model.entity.factory.FactoryType;
import com.cadify.cadifyWAS.model.entity.member.QOAuthMember;
import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import com.cadify.cadifyWAS.model.entity.order.QOrderItem;
import com.cadify.cadifyWAS.model.entity.order.QOrders;
import com.cadify.cadifyWAS.repository.util.QueryDslUtils;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Repository
public class FactoryOrderQueryRepositoryImpl implements FactoryOrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QOrders order = QOrders.orders;
    private static final QOrderItem orderItem = QOrderItem.orderItem;
    private static final QOAuthMember member = QOAuthMember.oAuthMember;

    public FactoryOrderQueryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    // Factory dashboard : 대시보드 카드 데이터 조회 [ 주문수(총, 제작중, 완료, 이번주), 매출금액(오늘, 이번주, 이번달) ]
    @Override
    public DashboardCardResponse getDashboardCardData(String factoryKey, FactoryType factoryType) {

        // 판금/절삭 공장 식별
        BooleanExpression factoryTypeExp = isOrderOfFactoryType(factoryType);
        // 조건별 시간 객체
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime thisWeek = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime thisMonth = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        // 주문수 Expression
        NumberExpression<Long> totalCount =
                countWithOrderStatus(order.orderKey, null, null);
        // status : 제작중
        NumberExpression<Long> inProductionCount =
                countWithOrderStatus(order.orderKey, order.orderReceivedStatus,
                        List.of(OrderReceivedStatus.CREATING));
        // status : 정산완료, 배송중, 배송완료
        NumberExpression<Long> completedCount =
                countWithOrderStatus(order.orderKey, order.orderReceivedStatus,
                        List.of(OrderReceivedStatus.SETTLED, OrderReceivedStatus.SHIPPING, OrderReceivedStatus.DELIVERED));
        NumberExpression<Long> thisWeekCount = new CaseBuilder()
                .when(order.createdAt.goe(thisWeek))
                .then(1L)
                .otherwise(0L)
                .sum()
                .coalesce(0L);
        // 매출금액 Expression
        NumberExpression<Integer> todayRevenue = QueryDslUtils.sumRevenueBetween(order.createdAt, order.totalPrice, today, today.plusDays(1));
        NumberExpression<Integer> weekRevenue = QueryDslUtils.sumRevenueBetween(order.createdAt, order.totalPrice, thisWeek, LocalDateTime.now());
        NumberExpression<Integer> monthRevenue = QueryDslUtils.sumRevenueBetween(order.createdAt, order.totalPrice, thisMonth, LocalDateTime.now());

        return queryFactory.select(Projections.constructor(DashboardCardResponse.class,
                        totalCount, inProductionCount, completedCount, thisWeekCount, todayRevenue, weekRevenue, monthRevenue))
                .from(order)
                .where(factoryTypeExp)
                .fetchOne();
    }

    // Factory dashboard : 월별 매출 차트 데이터
    @Override
    public List<Tuple> getMonthlyRevenueData(String factoryKey, FactoryType factoryType, LocalDateTime twelveMonthAgo) {
        BooleanExpression factoryTypeExp = isOrderOfFactoryType(factoryType);
        NumberExpression<Integer> year = order.createdAt.year();
        NumberExpression<Integer> month = order.createdAt.month();

        return queryFactory
                .select(
                        year.as("year"), month.as("month"),
                        order.totalPrice.sum().coalesce(0).as("revenue")
                ).from(order)
                .where(factoryTypeExp, order.createdAt.goe(twelveMonthAgo))
                .groupBy(year, month)
                .orderBy(order.createdAt.year().desc(), order.createdAt.month().desc())
                .fetch();
    }

    // 요일별 주문 수 ( 현재 주 )
    @Override
    public List<Tuple> getWeeklyOrderCount(String factoryKey, FactoryType factoryType, LocalDateTime sevenDaysAgo) {
        BooleanExpression factoryTypeExp = isOrderOfFactoryType(factoryType);
        DateTemplate<LocalDate> date = Expressions.dateTemplate(LocalDate.class, "date({0})", order.createdAt);
        return queryFactory
                .select(date, order.count())
                .from(order)
                .where(order.createdAt.goe(sevenDaysAgo), factoryTypeExp)
                .groupBy(date)
                .fetch();
    }

    // Factory order : 제작 대기 상태 주문 목록 조회
    @Override
    public List<OrderResponse> getOrderList(String factoryKey, FactoryType factoryType, List<OrderReceivedStatus> status) {
        BooleanExpression factoryTypeExp = isOrderOfFactoryType(factoryType);

        StringExpression methodsArr = Expressions.stringTemplate(
                "array_to_string(array_agg({0}) WITHIN GROUP (ORDER BY {0}), ',')", orderItem.method
        );
        return queryFactory
                .select(Projections.constructor(OrderResponse.class,
                        order.createdAt,
                        order.orderKey,
                        member.memberName,
                        methodsArr,  // DTO 에서 한글로 파싱
                        orderItem.countDistinct().coalesce(0L),
                        order.shipmentDate,
                        order.totalPrice,
                        order.orderReceivedStatus,
                        order.modifiedAt
                ))
                .from(order)
                .innerJoin(member).on(order.memberKey.eq(member.memberKey))
                .innerJoin(orderItem).on(order.orderKey.eq(orderItem.orderKey))
                .where(factoryTypeExp, statusCondition(status))
                .groupBy(
                        order.createdAt,
                        order.orderKey,
                        member.memberName,
                        order.shipmentDate,
                        order.totalPrice,
                        order.orderReceivedStatus
                )
                .orderBy(order.createdAt.desc())
                .fetch();
    }

    // 상태값에 따른 Order 에 속해있는 item 리스트 반환
    @Override
    public List<OrderItemResponse> getOrderItemLists(String factoryKey, FactoryType factoryType, String orderKey, List<OrderReceivedStatus> status) {
        BooleanExpression itemTypeExp = isItemOfFactoryType(factoryType);
        BooleanExpression orderStatusExp = (status != null && !status.isEmpty())
                ? orderItem.orderReceivedStatus.in(status) : null;

        return queryFactory
                .select(Projections.constructor(OrderItemResponse.class,
                        orderItem.orderItemKey, orderItem.fileName, orderItem.estName, orderItem.material, orderItem.method,
                        orderItem.thickness, orderItem.amount, orderItem.shipmentDate, orderItem.surface,
                        orderItem.estKey, orderItem.imageAddress, orderItem.orderReceivedStatus, orderItem.orderKey, orderItem.trackingNumber, orderItem.courier))
                .from(orderItem)
                .where(
                        itemTypeExp,
                        orderStatusExp,
                        orderItem.orderKey.eq(orderKey))
                .orderBy(orderItem.method.desc(), orderItem.shipmentDate.asc())
                .fetch();
    }

    // 주문 아이템 제작 가능 판정
    @Override
    public List<OrderItemResponse> confirmOrderItem(String factoryKey, FactoryType factoryType, FactoryOrderDTO.ConfirmRequest request) {
        // 변경전 주문 상태 확인
        checkOrderStatus(request.getOrderKey());
        // 업데이트
        long result = queryFactory
                .update(orderItem)
                .set(orderItem.orderReceivedStatus, OrderReceivedStatus.CREATING)
                .where(
                        orderItem.orderKey.eq(request.getOrderKey()),
                        orderItem.orderItemKey.eq(request.getItemKey()),
                        orderItem.orderReceivedStatus.eq(OrderReceivedStatus.PAID))
                .execute();
        if (result == 0) {
            throw new CustomLogicException(ExceptionCode.ITEM_UPDATE_FAILED);
        }
        // 제작대기 상태인 아이템 리스트만 리턴
        return getOrderItemLists(factoryKey, FactoryType.ALL, request.getOrderKey(), List.of(OrderReceivedStatus.PAID));
    }
    // 주문 거절: 가공 불가 판정
    @Override
    public RejectedItemResponse rejectOrderItem(String factoryKey, FactoryType factoryType, FactoryOrderDTO.RejectRequest request) {
        // 주문상태 확인
        checkOrderStatus(request.getOrderKey());
        // 업데이트
        long result = queryFactory
                .update(orderItem)
                .set(orderItem.orderReceivedStatus, OrderReceivedStatus.REJECT_REQUEST)
                .set(orderItem.rejectReasonCategory, request.getRejectReasonCategory())
                .set(orderItem.rejectReasonDetail, request.getRejectReasonDetail())
                .where(
                        orderItem.orderKey.eq(request.getOrderKey()),
                        orderItem.orderItemKey.eq(request.getItemKey()),
                        orderItem.orderReceivedStatus.eq(OrderReceivedStatus.PAID))
                .execute();
        if (result == 0) {
            throw new CustomLogicException(ExceptionCode.ITEM_UPDATE_FAILED);
        }
        // 주문상태 거절로 변경
        updateOrderStatus(request.getOrderKey(), OrderReceivedStatus.REJECT_REQUEST);

        return queryFactory
                .select(Projections.constructor(RejectedItemResponse.class,
                        orderItem.orderReceivedStatus, orderItem.rejectReasonCategory, orderItem.rejectReasonDetail))
                .from(orderItem)
                .where(orderItem.orderItemKey.eq(request.getItemKey()))
                .fetchOne();
    }

    // 송장 등록 & 상태 배송중 업데이트 후 제작중인 item 리스트 반환
    @Override
    public List<OrderItemResponse> startShippingAndReturnRemainedItems(String factoryKey, FactoryType factoryType, FactoryOrderDTO.StartShippingRequest request) {

        // 아이템 리스트 상태 업데이트
        long result = queryFactory.update(orderItem)
                .set(orderItem.orderReceivedStatus, OrderReceivedStatus.SHIPPING)
                .set(orderItem.trackingNumber, request.getTrackingNumber())
                .set(orderItem.courier, request.getCourier())
                .where(
                        orderItem.orderKey.eq(request.getOrderKey()),
                        orderItem.orderItemKey.in(request.getItemKeyList()),
                        orderItem.orderReceivedStatus.eq(OrderReceivedStatus.CREATING))
                .execute();
        if (result == 0) {
            throw new CustomLogicException(ExceptionCode.ITEM_UPDATE_FAILED);
        }
        // 제작중 상태인 아이템 리스트만 리턴
        return getOrderItemLists(factoryKey, factoryType, request.getOrderKey(), List.of(OrderReceivedStatus.CREATING));
    }

    // Factory shipment : 날짜별 납기일 주문 수 조회
    @Override
    public List<CalendarOrderCountsResponse> getShipmentDateOrderCount(String factoryKey, FactoryType factoryType, List<String> yearMonth) {
        BooleanExpression factoryTypeExp = isOrderOfFactoryType(factoryType);

        StringTemplate shipmentStr = Expressions.stringTemplate("TO_CHAR({0}, 'YYYY-MM-DD')", order.shipmentDate);

        LocalDate startDate = YearMonth.parse(yearMonth.get(0)).atDay(1);
        LocalDate endDate = YearMonth.parse(yearMonth.get(yearMonth.size()-1)).atEndOfMonth();

        return queryFactory
                .select(Projections.constructor(CalendarOrderCountsResponse.class,
                        shipmentStr, order.orderKey.count().coalesce(0L)))
                .from(order)
                .where(
                        factoryTypeExp,
                        order.shipmentDate.between(startDate, endDate),
                        order.orderReceivedStatus.in(OrderReceivedStatus.PAID, OrderReceivedStatus.CREATING))
                .groupBy(shipmentStr)
                .fetch();
    }


    // 최근 정산내역 조회
    @Override
    public List<OrderResponse> getRecentlySettlementOrders(String factoryKey, FactoryType factoryType) {
        BooleanExpression factoryTypeExp = isOrderOfFactoryType(factoryType);

        StringExpression methodsArr = Expressions.stringTemplate(
                "array_to_string(array_agg({0}) WITHIN GROUP (ORDER BY {0}), ',')", orderItem.method
        );
        return queryFactory
                .select(Projections.constructor(OrderResponse.class,
                        order.createdAt,
                        order.orderKey,
                        member.memberName,
                        methodsArr,  // DTO 에서 한글로 파싱
                        orderItem.countDistinct().coalesce(0L),
                        order.shipmentDate,
                        order.totalPrice,
                        order.orderReceivedStatus
                ))
                .from(order)
                .innerJoin(member).on(order.memberKey.eq(member.memberKey))
                .innerJoin(orderItem).on(order.orderKey.eq(orderItem.orderKey))
                .where(factoryTypeExp, statusCondition(List.of(OrderReceivedStatus.SETTLED)))
                .groupBy(
                        order.createdAt,
                        order.orderKey,
                        member.memberName,
                        order.shipmentDate,
                        order.totalPrice,
                        order.orderReceivedStatus
                )
                .orderBy(order.modifiedAt.desc())
                .fetch();
    }
    // 정산 대시보드 카드 데이터
    @Override
    public SettlementCardsResponse getSettlementCardsData(String factoryKey, FactoryType factoryType) {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        BooleanExpression factoryTypeExp = isOrderOfFactoryType(factoryType);

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
                .where(factoryTypeExp)
                .from(order)
                .fetchOne();
    }

    // 단일 주문 상태 업데이트
    @Override
    public void updateOrderStatus(String orderKey, OrderReceivedStatus status) {
        long result = queryFactory.update(order)
                .set(order.orderReceivedStatus, status)
                .where(order.orderKey.eq(orderKey))
                .execute();
        // 업데이트 된 레코드가 0 -> 주문키가 다르거나 이미 상태가 업데이트 되어있음
        if (result == 0) {
            throw new CustomLogicException(ExceptionCode.ORDER_UPDATE_FAILED);
        }
    }

//  ------------------ 내부 Util

    // OrderItem 업데이트 전 Order 상태 확인
    private void checkOrderStatus(String orderKey) {
        OrderReceivedStatus orderStatus = queryFactory
                .select(order.orderReceivedStatus)
                .from(order)
                .where(order.orderKey.eq(orderKey))
                .fetchOne();

        // 존재하지 않는 주문
        if (orderStatus == null) {
            throw new CustomLogicException(ExceptionCode.ORDER_NOT_FOUND);
        }

        switch (orderStatus) {
            // 이미 취소된 주문
            case CANCELLED -> throw new CustomLogicException(ExceptionCode.ORDER_ALREADY_CANCELLED);
            // 이미 거절된 주문
            case REJECTED -> throw new CustomLogicException(ExceptionCode.ORDER_ALREADY_REJECTED);
            // 이미 배송중인 주문
            case SHIPPING -> throw new CustomLogicException(ExceptionCode.ORDER_ALREADY_SHIPPING);
        }
    }

    // OrderItem 의 가공타입을 비교해서 (cnc 혹은 sheet_metal 을 포함하는지 ) 해당하는 Order 만 필터링
    private BooleanExpression isOrderOfFactoryType(FactoryType type) {
        if (type == null) {
            throw new CustomLogicException(ExceptionCode.INVALID_FACTORY_TYPE);
        }

        // Factory Type 기준 orderItem method(가공방법) 필터링
        return switch (type) {
            case SHEET_METAL -> order.orderKey.in(
                    JPAExpressions
                            .select(orderItem.orderKey)
                            .from(orderItem)
                            .where(orderItem.method.upper().eq("SHEET_METAL"))
            );
            case CNC -> order.orderKey.in(
                    JPAExpressions
                            .select(orderItem.orderKey)
                            .from(orderItem)
                            .where(orderItem.method.upper().eq("CNC"))
            );
            case ALL -> null;
        };
    }

    private BooleanExpression isItemOfFactoryType(FactoryType type){
        if( type == null ){
            throw new CustomLogicException(ExceptionCode.INVALID_FACTORY_TYPE);
        }

        return switch (type) {
            case SHEET_METAL -> orderItem.method.upper().eq("SHEET_METAL");
            case CNC -> orderItem.method.upper().eq("CNC");
            case ALL -> null;
        };
    }

    // orderReceivedStatus 기준 카운터
    private NumberExpression<Long> countWithOrderStatus(
            StringPath keyPath,
            EnumPath<OrderReceivedStatus> statusPath,
            List<OrderReceivedStatus> values) {
        if (values == null || values.isEmpty()) {
            return keyPath.count().coalesce(0L);
        }

        return new CaseBuilder()
                .when(statusPath.in(values))
                .then(1L)
                .otherwise(0L)
                .sum()
                .coalesce(0L);
    }

    private BooleanExpression statusCondition(List<OrderReceivedStatus> status) {
        if (status == null || status.isEmpty()) {
            return null;
        } else {
            return order.orderReceivedStatus.in(status);
        }
    }
}
