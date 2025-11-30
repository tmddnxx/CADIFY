package com.cadify.cadifyWAS.model.dto.company;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompanyManagerResponse {
    private String managerKey;
    private String department;
    private String position;
    private String phone;
    private String managerName;

    public CompanyManagerResponse(String managerKey, String phone, String department, String position, String managerName){
        this.managerKey = managerKey;
        this.phone = phone;
        this.department = department;
        this.position = position;
        this.managerName = managerName;
    }
}
