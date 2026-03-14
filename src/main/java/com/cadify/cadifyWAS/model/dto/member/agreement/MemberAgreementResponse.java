package com.cadify.cadifyWAS.model.dto.member.agreement;

import com.cadify.cadifyWAS.model.entity.member.MemberAgreementType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.format.ISODateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Getter
@NoArgsConstructor
public class MemberAgreementResponse {
    private String agreementType;
    private Boolean agreed;
    private String agreedAt;

    @Builder
    public MemberAgreementResponse(MemberAgreementType agreementType, Boolean agreed, LocalDateTime agreedAt){
        this.agreementType = agreementType.name();
        this.agreed = agreed;
        this.agreedAt = agreedAt.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
