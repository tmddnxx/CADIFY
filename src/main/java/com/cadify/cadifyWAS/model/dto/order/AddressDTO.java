package com.cadify.cadifyWAS.model.dto.order;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AddressDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request{

        private String address;

        private String addressDetail;

        private String addressLabel;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateRequest{
        private String address;

        private String addressDetail;

        private String addressLabel;
    }

    @Getter
    @Setter
    @Builder
    public static class Response{

        private String addressKey;

        private String address;

        private String addressDetail;

        private String addressLabel;

    }
}
