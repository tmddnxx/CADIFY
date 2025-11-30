package com.cadify.cadifyWAS.repository.admin.estimate;

import com.cadify.cadifyWAS.model.entity.Files.QEstimate;
import com.cadify.cadifyWAS.model.entity.Files.QFiles;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdminEstimateQueryRepositoryImpl implements AdminEstimateQueryRepository {
    private final JPAQueryFactory queryFactory;
    private static final QEstimate estimate = QEstimate.estimate;
    private static final QFiles files = com.cadify.cadifyWAS.model.entity.Files.QFiles.files;

    public AdminEstimateQueryRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<Tuple> getEstimatesWithFiles() {
        return queryFactory.select(estimate, files.s3StepAddress, files.imageAddress)
                .from(estimate)
                .join(files)
                .on(estimate.fileId.eq(files.id))
                .fetch();
    }

    @Override
    public Tuple getEstimateWithFilesByKey(String estKey) {
        return queryFactory.select(estimate, files.s3StepAddress, files.imageAddress)
                .from(estimate)
                .join(files)
                .on(estimate.fileId.eq(files.id))
                .where(estimate.estKey.eq(estKey))
                .fetchOne();
    }

    // todo : 이곳에 관리자 견적 관련 쿼리 메소드를 구현합니다.
}
