package com.cadify.cadifyWAS.model.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RevenueCardResponse {
    private RevenueCardTemp day;
    private RevenueCardTemp week;
    private RevenueCardTemp month;
}
