package com.cadify.cadifyWAS.model.dto.factory.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeeklyOrderCountResponse {
    private String dayOfWeek;
    private Long orderCount;

    public WeeklyOrderCountResponse(String dayOfWeek, Long orderCount){
        this.dayOfWeek = dayOfWeek;
        this.orderCount = orderCount;
    }
}
