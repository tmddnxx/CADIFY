package com.cadify.cadifyWAS.model.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevenueCardResponse {
    private RevenueCardTemp day;
    private RevenueCardTemp week;
    private RevenueCardTemp month;
}
