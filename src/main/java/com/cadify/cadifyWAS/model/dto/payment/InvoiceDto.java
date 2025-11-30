package com.cadify.cadifyWAS.model.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class InvoiceDto {

    @Getter
    @NoArgsConstructor
    public static class Request{
        @NotBlank(message = "회사명은 필수 입니다")
        private String companyName;
        @NotBlank(message = "인수 담당자 이름은 필수입니다")
        private String receiverName;
    }
}
