package com.cadify.cadifyWAS.model.dto.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class FactoryAdminDTO {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class JoinRequest{
        private String username;
        private String password;
        private String name;
        private String email;
        private String phone;
        private String factoryKey;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InfoResponse{
        private String username;
        private String email;
        private String phone;
    }
}
