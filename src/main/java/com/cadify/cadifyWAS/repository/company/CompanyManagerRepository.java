package com.cadify.cadifyWAS.repository.company;

import com.cadify.cadifyWAS.model.entity.member.CompanyManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyManagerRepository extends JpaRepository<CompanyManager, String>, CompanyManagerQueryRepository {

    Optional<CompanyManager>findByManagerKey(String managerKey);

}
