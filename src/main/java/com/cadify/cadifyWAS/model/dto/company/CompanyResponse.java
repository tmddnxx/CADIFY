package com.cadify.cadifyWAS.model.dto.company;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompanyResponse {
    private String companyName;
    private String companyEmail;

    public CompanyResponse(String companyName, String companyEmail){
        this.companyName = companyName;
        this.companyEmail = companyEmail;
    }
}
