package com.cadify.cadifyWAS.model.dto.admin.member;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@Getter
public class FilteredMemberResponse {
    private String name;
    private String email;
    private String phone;
    private Long orderCount;
    private String joined;
    private Integer amount;

    public FilteredMemberResponse(String name, String email, String phone, Long orderCount, LocalDateTime joined, Integer amount){
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.orderCount = orderCount;
        this.joined = joined.format(DateTimeFormatter.ISO_DATE);
        this.amount = amount;
    }
}
