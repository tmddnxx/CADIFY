package com.cadify.cadifyWAS.repository.member;

import com.cadify.cadifyWAS.model.entity.member.MemberAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAgreementRepository extends JpaRepository<MemberAgreement, Long>, MemberAgreementQueryRepository {
}
