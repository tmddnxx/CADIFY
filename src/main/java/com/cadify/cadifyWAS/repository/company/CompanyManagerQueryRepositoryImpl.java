package com.cadify.cadifyWAS.repository.company;

import com.cadify.cadifyWAS.model.dto.company.CompanyManagerResponse;
import com.cadify.cadifyWAS.model.entity.member.QCompanyManager;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CompanyManagerQueryRepositoryImpl implements CompanyManagerQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QCompanyManager manager = QCompanyManager.companyManager;

    public CompanyManagerQueryRepositoryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<CompanyManagerResponse> findCompanyManagerList(String memberKey) {
        return queryFactory.select(Projections.constructor(CompanyManagerResponse.class,
                manager.managerKey, manager.phone, manager.department, manager.position, manager.managerName))
                .from(manager)
                .where(manager.memberKey.eq(memberKey))
                .fetch();
    }
}
