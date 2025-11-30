package com.cadify.cadifyWAS.repository.factory;

import com.cadify.cadifyWAS.model.entity.factory.Factory;
import com.cadify.cadifyWAS.model.entity.factory.QFactory;
import com.cadify.cadifyWAS.model.entity.factory.QFactoryAdmin;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class FactoryQueryRepositoryImpl implements FactoryQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QFactory factory = QFactory.factory;
    private final QFactoryAdmin factoryAdmin = QFactoryAdmin.factoryAdmin;

    public FactoryQueryRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Optional<Factory> getFactoryFromPrincipal(String memberKey) {
        Factory result = queryFactory
                .select(factory)
                .from(factory)
                .innerJoin(factoryAdmin).on(factory.factoryKey.eq(factoryAdmin.factoryKey))
                .where(factoryAdmin.memberKey.eq(memberKey))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
