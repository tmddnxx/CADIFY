package com.cadify.cadifyWAS.model.dto.member;

import lombok.Builder;
import lombok.Getter;

public class MemberDTO {

    @Builder
    @Getter
    public static class MemberInfo{
        private String memberName;
        private String email;
        private String phone;
        private Integer addressNumber;
        private String addressDetail;
        private String role;

        public MemberInfo(String memberName, String email, String phone, Integer addressNumber, String addressDetail, String role){
            this.memberName = memberName;
            this.email = email;
            this.phone = phone;
            this.addressNumber = addressNumber;
            this.addressDetail = addressDetail;
            this.role = role;
        }
    }

    @Builder
    @Getter
    public static class UpdateMember{
        private String memberName;
    }
}