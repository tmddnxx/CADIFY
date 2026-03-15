package com.cadify.cadifyWAS.model.dto.company;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    // 관리자 삭제 요청
    @Getter
    @Setter
    @NoArgsConstructor
    public static class DeleteManagerRequest {
        private String managerKey;
    }

    // 회사 정보 수정 요청
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateCompanyInfoRequest {
        private String companyName;
    }
}
