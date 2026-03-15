package com.cadify.cadifyWAS.repository.member;

import com.cadify.cadifyWAS.model.dto.member.agreement.MemberAgreementResponse;
import com.cadify.cadifyWAS.model.entity.member.MemberAgreementType;
import com.cadify.cadifyWAS.model.entity.member.QMemberAgreement;
import com.cadify.cadifyWAS.model.entity.member.QOAuthMember;
import com.cadify.cadifyWAS.repository.util.QueryDslUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MemberAgreementQueryRepositoryImpl implements MemberAgreementQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final QMemberAgreement agreement = QMemberAgreement.memberAgreement;
    private static final QOAuthMember member = QOAuthMember.oAuthMember;

    public MemberAgreementQueryRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<MemberAgreementResponse> getPersonalAgreements(String memberKey, List<MemberAgreementType> agreements) {

        QMemberAgreement subAgreement = new QMemberAgreement("subAgreement");

        return queryFactory
                .select(Projections.constructor(MemberAgreementResponse.class,
                        agreement.memberAgreementType, agreement.agreed, agreement.agreedAt))
                .from(agreement)
                .innerJoin(member).on(member.memberKey.eq(agreement.memberKey))
                .where(
                        member.memberKey.eq(memberKey),
                        QueryDslUtils.isMemberNotDeleted(member),
                        agreement.memberAgreementType.in(agreements),
                        agreement.agreedAt.eq(
                                JPAExpressions
                                        .select(subAgreement.agreedAt.max())
                                        .from(subAgreement)
                                        .where(
                                                subAgreement.memberKey.eq(memberKey),
                                                subAgreement.memberAgreementType.eq(agreement.memberAgreementType))
                        ))
                .fetch();
    }
}
