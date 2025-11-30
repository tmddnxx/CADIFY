package com.cadify.cadifyWAS.model.dto.admin.dashboard;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopRevenueMemberResponse {
    private String name;
    private Integer revenue;
}

