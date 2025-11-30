package com.cadify.cadifyWAS.repository.util;

import com.cadify.cadifyWAS.model.entity.member.QOAuthMember;
import com.querydsl.core.types.dsl.*;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

public class QueryDslUtils {
    // 동일한 스트링 검증 ( 없거나, ALL 일경우 패스 )
    public static BooleanExpression eqString(StringPath path, String value) {
        if (!StringUtils.hasText(value) || "ALL".equalsIgnoreCase(value)) {
            return null;
        }
        return path.eq(value);
    }
    // 동일한 열거형 인지 검증 ( 단일 )
    public static <T extends Enum<T>> BooleanExpression eqEnum(EnumPath<T> path, T value){
        if (value == null) return null;
        return path.eq(value);
    }
    // 해당 열거형 포함하는지 검증 ( 2개 이상 )
    public static <T extends Enum<T>> BooleanExpression eqEnumArr(EnumPath<T> path, List<T> value){
        if (value == null || value.isEmpty()) return null;
        return path.in(value);
    }
    // 특정 기간 중 revenue 합 반환
    public static NumberExpression<Integer> sumRevenueBetween(
            DateTimePath<LocalDateTime> path, NumberPath<Integer> price, LocalDateTime start, LocalDateTime end) {
        return new CaseBuilder()
                .when(path.goe(start).and(path.lt(end)))
                .then(price)
                .otherwise(0)
                .sum()
                .coalesce(0);
    }
    // 탈퇴 or 삭제된 사용자 필터링
    public static BooleanExpression isMemberNotDeleted(QOAuthMember member){
        return member.deleted.isFalse();
    }
}
