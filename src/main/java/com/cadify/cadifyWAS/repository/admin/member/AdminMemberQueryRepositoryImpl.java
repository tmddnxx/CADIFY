package com.cadify.cadifyWAS.repository.admin.member;

import com.cadify.cadifyWAS.model.dto.admin.dashboard.MemberCardResponse;
import com.cadify.cadifyWAS.model.dto.admin.dashboard.TopRevenueMemberResponse;
import com.cadify.cadifyWAS.model.dto.admin.member.AdminMemberDTO;
import com.cadify.cadifyWAS.model.dto.admin.member.FilteredMemberResponse;
import com.cadify.cadifyWAS.model.dto.admin.member.PersonalOrderResponse;
import com.cadify.cadifyWAS.model.entity.member.QOAuthMember;
import com.cadify.cadifyWAS.model.entity.order.QOrders;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AdminMemberQueryRepositoryImpl implements AdminMemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QOAuthMember member = QOAuthMember.oAuthMember;
    private final QOrders order = QOrders.orders;

    public AdminMemberQueryRepositoryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    // Admin Dashboard 회원관련 카드 데이터 조회
    @Override
    public MemberCardResponse findMemberCardStatus(LocalDateTime today, LocalDateTime thisWeek) {
        NumberExpression<Long> newMembers = new CaseBuilder()
                .when(member.createdAt.goe(today)).then(1L).otherwise(0L).sum().coalesce(0L);

        NumberExpression<Long> weeklyMembers = new CaseBuilder()
                .when(member.createdAt.goe(thisWeek)).then(1L).otherwise(0L).sum().coalesce(0L);

        return queryFactory
                .select(Projections.constructor(MemberCardResponse.class,
                        member.count().coalesce(0L), newMembers, weeklyMembers
                ))
                .from(member)
                .fetchOne();
    }
    // 조건별 회원 조회
    @Override
    public List<FilteredMemberResponse> findFilteredMembersData(AdminMemberDTO.FilteredMemberRequest request){

        BooleanExpression nameCondition = StringUtils.hasText(request.getCompanyName()) ? member.memberName.eq(request.getCompanyName()) : null;

        return queryFactory
                .select(Projections.constructor(FilteredMemberResponse.class,
                        member.memberName, member.email, member.phone,
                        order.count().as("orderCount"),
                        member.createdAt.as("joined"),
                        order.totalPrice.sum().coalesce(0).as("amount")
                ))
                .from(member)
                .leftJoin(order).on(order.memberKey.eq(member.memberKey))
                .where(
                        nameCondition,
                        searchCondition(request.getSearch()),
                        getJoinedExpression(request.getJoined())
                )
                .groupBy(member.memberKey)
                .having(
                        getOrderCountExpression(request.getOrderCount())
                )
                .orderBy(getSortBy(request.getSort()))
                .fetch();
    }

    // 회원별 주문 내역 조회
    @Override
    public List<PersonalOrderResponse> findPersonalOrderList(String email) {
        return queryFactory.select(Projections.constructor(PersonalOrderResponse.class,
                order.createdAt, order.orderKey, order.shipmentDate, order.totalPrice, order.orderReceivedStatus))
                .from(order)
                .innerJoin(member).on(order.memberKey.eq(member.memberKey))
                .where(member.email.eq(email))
                .orderBy(order.createdAt.desc())
                .fetch();
    }

    // Admin dashboard : 최고 매출 회원 차트 TOP 5
    @Override
    public List<TopRevenueMemberResponse> findTopRevenueMembers() {

        NumberExpression<Integer> revenue = order.totalPrice.sum().coalesce(0);

        return queryFactory.select(
                        Projections.constructor(TopRevenueMemberResponse.class,
                                member.memberName, revenue))
                .from(member)
                .leftJoin(order).on(order.memberKey.eq(member.memberKey))
                .groupBy(member.memberName)
                .orderBy(revenue.desc())
                .limit(5)
                .fetch();
    }


// BEGIN:
    // 주문수 필터
    private BooleanExpression getOrderCountExpression(String orderCount){
        if (!StringUtils.hasText(orderCount) || "all".equalsIgnoreCase(orderCount)) return null;

        return switch (orderCount) {
            case "0-10" -> order.count().coalesce(0L).loe(10L);
            case "11-30" -> order.count().goe(11L).and(order.count().loe(30L));
            case "31+" -> order.count().goe(31L);
            default -> null;
        };
    }
    // 가입일 필터
    private BooleanExpression getJoinedExpression(String joined){
        if(!StringUtils.hasText(joined) || "all".equalsIgnoreCase(joined)) return null;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target;

        switch (joined){
            case "1m": target = now.minusMonths(1); break;
            case "3m": target = now.minusMonths(3); break;
            case "6m": target = now.minusMonths(6); break;
            case "1y": target = now.minusYears(1); break;
            default: return null;
        }

        return member.createdAt.goe(target);
    }
    // 정렬 필터
    private OrderSpecifier<?> getSortBy(String sort){

        if(!StringUtils.hasText(sort)) return member.memberName.asc();

        return switch(sort){
            case "orderCount" -> order.count().desc();
            case "amount" -> order.totalPrice.sum().coalesce(0).desc();
            case "joined" -> member.createdAt.desc();
            default -> member.memberName.asc();
        };
    }
    // 검색어 조건
    private BooleanExpression searchCondition(String search){
        if (!StringUtils.hasText(search)) return null;
        return member.memberName.containsIgnoreCase(search)
                .or(member.email.containsIgnoreCase(search))
                .or(member.phone.containsIgnoreCase(search));
    }
// END

}