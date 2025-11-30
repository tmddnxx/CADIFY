package com.cadify.cadifyWAS.model.dto.admin.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AdminMemberDTO {

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class FilteredMemberRequest {
        private String companyName = "";
        private String orderCount = "";
        private String joined = "";
        private String sort = "name";
        private String search = "";
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class PersonalOrderRequest {
        private String email;
    }

}
