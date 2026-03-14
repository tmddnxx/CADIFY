package com.cadify.cadifyWAS.model.dto.admin.order;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CalendarOrderCountsResponse {
    private String date;
    private Long orderCount;

    @Builder
    public CalendarOrderCountsResponse(String date, Long orderCount){
        this.date = date;
        this.orderCount = orderCount;
    }
}
