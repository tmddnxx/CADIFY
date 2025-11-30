package com.cadify.cadifyWAS.repository.factory.estimate;

import com.querydsl.core.Tuple;

public interface FactoryEstimateQueryRepository {
    // todo: 이곳에 공장 견적 관련 쿼리 메소드를 정의합니다.
    Tuple findEstimateByorderItemKey(String orderItemKey);
}
