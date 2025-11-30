package com.cadify.cadifyWAS.repository.company;

import com.cadify.cadifyWAS.model.dto.company.CompanyManagerResponse;

import java.util.List;

public interface CompanyManagerQueryRepository {
    List<CompanyManagerResponse> findCompanyManagerList(String memberKey);
}
