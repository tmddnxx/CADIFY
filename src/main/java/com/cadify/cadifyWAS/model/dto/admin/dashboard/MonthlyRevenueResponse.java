package com.cadify.cadifyWAS.model.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyRevenueResponse {
    private Integer month;
    private Integer revenue;
}
