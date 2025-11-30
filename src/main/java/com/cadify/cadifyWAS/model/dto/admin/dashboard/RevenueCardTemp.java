package com.cadify.cadifyWAS.model.dto.admin.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RevenueCardTemp {
    private Integer revenue;
    private String percent;

    public RevenueCardTemp(Integer revenue, Integer previous){
        this.revenue = revenue;
        this.percent = calcPercents(revenue, previous);
    }

    private String calcPercents(Integer current, Integer previous){
        if (current == 0 && previous == 0) {
            return "--";
        }
        if (current == 0) {
            return "-Infinity%";
        }
        if (previous == 0) {
            return "+Infinity%";
        }

        double result = ((current - (double) previous) / previous) * 100;
        String sign = result >= 0 ? "+" : "";

        return sign + String.format("%.2f%%", result);
    }
}
