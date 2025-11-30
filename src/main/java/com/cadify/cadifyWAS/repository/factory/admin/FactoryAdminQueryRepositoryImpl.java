package com.cadify.cadifyWAS.repository.factory.admin;

import com.cadify.cadifyWAS.model.entity.factory.Factory;
import com.cadify.cadifyWAS.model.entity.factory.QFactory;
import com.cadify.cadifyWAS.model.entity.factory.QFactoryAdmin;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class FactoryAdminQueryRepositoryImpl implements FactoryAdminQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QFactory qFactory = QFactory.factory;
    private final QFactoryAdmin qAdmin = QFactoryAdmin.factoryAdmin;

    public FactoryAdminQueryRepositoryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 인증 객체의 memberKey 로 소속 공장 조회 ( memberKey -> qAdmin -> qFactory )
    @Override
    public Factory findFactoryByMemberKey(String memberKey){
        return queryFactory
                .select(qFactory)
                .from(qAdmin)
                .join(qFactory).on(qAdmin.factoryKey.eq(qFactory.factoryKey))
                .where(qAdmin.memberKey.eq(memberKey))
                .fetchFirst();
    }
}
