package com.cadify.cadifyWAS.model.dto.factory.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardCardResponse {
    private Long totalOrders;
    private Long inProductions;
    private Long completedOrders;
    private Long thisWeekOrders;
    private Integer monthRevenue;
    private Integer weekRevenue;
    private Integer todayRevenue;
}