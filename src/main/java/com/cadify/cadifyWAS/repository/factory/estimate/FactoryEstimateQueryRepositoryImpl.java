package com.cadify.cadifyWAS.repository.factory.estimate;

import com.cadify.cadifyWAS.model.entity.Files.QEstimate;
import com.cadify.cadifyWAS.model.entity.Files.QFiles;
import com.cadify.cadifyWAS.model.entity.order.QOrderItem;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class FactoryEstimateQueryRepositoryImpl implements FactoryEstimateQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QEstimate estimate = QEstimate.estimate;
    private static final QFiles QFiles = com.cadify.cadifyWAS.model.entity.Files.QFiles.files;
    private static final QOrderItem orderItem = QOrderItem.orderItem;

    public FactoryEstimateQueryRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }
    // todo: 이곳에 공장 견적 관련 쿼리 메소드를 구현합니다.
    
    // 단일 견적 조회
    @Override
    public Tuple findEstimateByorderItemKey(String orderItemKey) {
        return queryFactory
                .select(orderItem, QFiles.factoryDxfAddress, QFiles.s3DxfAddress, QFiles.imageAddress, QFiles.s3StepAddress)
                .from(orderItem)
                .join(estimate).on(orderItem.estKey.eq(estimate.estKey))
                .join(QFiles).on(estimate.fileId.eq(QFiles.id))
                .where(orderItem.orderItemKey.eq(orderItemKey))
                .fetchFirst();
    }
    

}
