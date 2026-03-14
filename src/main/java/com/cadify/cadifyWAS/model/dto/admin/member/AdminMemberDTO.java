package com.cadify.cadifyWAS.model.dto.admin.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AdminMemberDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
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
