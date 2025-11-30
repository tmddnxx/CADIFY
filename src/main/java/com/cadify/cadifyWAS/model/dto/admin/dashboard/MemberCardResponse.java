package com.cadify.cadifyWAS.model.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberCardResponse {
    private Long totalValues = 0L;
    private Long newValues = 0L;
    private Long weeklyValues = 0L;
}
