package com.cadify.cadifyWAS.model.dto.member.agreement;

import com.cadify.cadifyWAS.model.entity.member.MemberAgreementType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class MemberAgreementDTO {

    @Getter
    @NoArgsConstructor
    public static class AgreementRequest{
        private MemberAgreementType agreementType;
        private Boolean agreed;

        public AgreementRequest(String agreementType, Boolean agreed){
            this.agreementType = MemberAgreementType.fromString(agreementType);
            this.agreed = agreed;
        }
    }
}
