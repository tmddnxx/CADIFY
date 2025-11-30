package com.cadify.cadifyWAS.repository.admin.estimate;

import com.querydsl.core.Tuple;

import java.util.List;

public interface AdminEstimateQueryRepository {
    // todo: 이곳에 관리자 견적 관련 쿼리 메소드를 정의합니다.
    List<Tuple> getEstimatesWithFiles();

    Tuple getEstimateWithFilesByKey(String estKey);
}
