package com.cadify.cadifyWAS.repository.admin.orderItem;

import com.cadify.cadifyWAS.model.dto.admin.dashboard.PreferredProcessingTypeResponse;
import com.cadify.cadifyWAS.model.dto.admin.order.AdminOrderDTO;
import com.cadify.cadifyWAS.model.dto.admin.order.OrderItemRes;
import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import com.cadify.cadifyWAS.model.entity.order.QOrderItem;
import com.cadify.cadifyWAS.repository.util.QueryDslUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class AdminOrderItemQueryRepositoryImpl implements AdminOrderItemQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QOrderItem item = QOrderItem.orderItem;

    public AdminOrderItemQueryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 관리자 페이지 공용 Order Item 조회 ( Order key )
    @Override
    public List<OrderItemRes> findOrderItemsByOrderKey(AdminOrderDTO.OrderItemRequest request) {

        BooleanExpression statusCondition = QueryDslUtils.eqEnum(item.orderReceivedStatus, request.getStatus());

        return queryFactory
                .select(Projections.constructor(OrderItemRes.class,
                        item.orderItemKey, item.fileName, item.estKey, item.estName, item.material, item.method,
                        item.thickness, item.surface, item.amount, item.orderReceivedStatus, item.rejectReasonDetail))
                .from(item)
                .where(item.orderKey.eq(request.getKey()), statusCondition)
                .fetch();
    }

    // Admin dashboard : 선호 가공 타입 조회
    @Override
    public PreferredProcessingTypeResponse findPreferredProcessingType() {

        NumberExpression<Integer> metal = new CaseBuilder().when(item.method.upper().eq("SHEET_METAL")).then(item.amount)
                .otherwise(0).sum().coalesce(0);
        NumberExpression<Integer> cnc = new CaseBuilder().when(item.method.upper().eq("CNC")).then(item.amount)
                .otherwise(0).sum().coalesce(0);

        return queryFactory
                .select(Projections.constructor(PreferredProcessingTypeResponse.class,
                        metal, cnc))
                .from(item)
                .fetchOne();
    }

    @Override
    public Set<OrderReceivedStatus> findDistinctItemStatus(String orderKey) {
        return new HashSet<>(
                queryFactory
                        .select(item.orderReceivedStatus).distinct()
                        .from(item)
                        .where(item.orderKey.eq(orderKey))
                        .fetch()
        );
    }
}
