package com.cadify.cadifyWAS.model.dto.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class FactoryAdminDTO {
    @Getter
    @AllArgsConstructor
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
    public static class InfoResponse{
        private String username;
        private String email;
        private String phone;
    }
}
