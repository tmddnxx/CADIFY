package com.cadify.cadifyWAS.model.dto.company;

import lombok.Getter;

public class CompanyDTO {

    @Getter
    public static class ConvertToCompanyRequest{
        private String companyName;
        private String department;
        private String position;
        private String managerName;
        private String phone;
        private String authCode;
    }

    @Getter
    public static class RegisterManager{
        private String department;
        private String position;
        private String managerName;
        private String phone;
        private String authCode;
    }

    @Getter
    public static class UpdateManager{
        private String managerKey;
        private String department;
        private String position;
        private String managerName;
    }
}
