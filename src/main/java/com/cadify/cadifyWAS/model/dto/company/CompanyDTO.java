package com.cadify.cadifyWAS.model.dto.company;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class CompanyDTO {

    @Getter
    @NoArgsConstructor
    public static class ConvertToCompanyRequest{
        private String companyName;
        private String department;
        private String position;
        private String managerName;
        private String phone;
        private String authCode;
    }

    @Getter
    @NoArgsConstructor
    public static class RegisterManager{
        private String department;
        private String position;
        private String managerName;
        private String phone;
        private String authCode;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateManager{
        private String managerKey;
        private String department;
        private String position;
        private String managerName;
    }
}
