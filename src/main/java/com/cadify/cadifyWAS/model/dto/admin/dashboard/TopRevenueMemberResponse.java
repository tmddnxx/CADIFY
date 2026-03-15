package com.cadify.cadifyWAS.model.dto.admin.dashboard;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TopRevenueMemberResponse {
    private String name;
    private Integer revenue;
}

