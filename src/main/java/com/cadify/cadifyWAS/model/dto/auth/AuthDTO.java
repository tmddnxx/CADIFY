package com.cadify.cadifyWAS.model.dto.auth;

import com.cadify.cadifyWAS.model.dto.member.MemberDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthDTO {

    @Builder
    @Getter
    public static class RefreshTokenInfo{
        private String refreshToken;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VerifyAuthCodeRequestDTO{
        private String phone;
        private String authCode;
    }

    @Getter
    @NoArgsConstructor
    public static class PhoneAuthRequestDTO{
        private String phone;
    }

    @Builder
    @Getter
    public static class AuthSMSResponse{
        private String authCode;
        private String expiredAt;
    }

    @Getter
    public static class AssignRoleResult {
        private final MemberDTO.MemberInfo memberInfo;
        private final String newAccessToken;

        public AssignRoleResult(MemberDTO.MemberInfo memberInfo, String newAccessToken){
            this.memberInfo = memberInfo;
            this.newAccessToken = newAccessToken;
        }
    }
}