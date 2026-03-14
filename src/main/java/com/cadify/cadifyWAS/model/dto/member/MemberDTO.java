package com.cadify.cadifyWAS.model.dto.member;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MemberDTO {

    @Builder
    @Getter
    @NoArgsConstructor
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

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateMember{
        private String memberName;
    }

    // 최초 로그인 시 역할 할당 요청
    @Getter
    @Setter
    @NoArgsConstructor
    public static class AssignRoleRequest {
        private String role;
    }
}