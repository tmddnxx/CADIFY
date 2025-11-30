package com.cadify.cadifyWAS.service.file.enumValues.metal.priceValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalCostByRBend {

    LEN100(0.0,100, 3000),

    LEN300(100.0,300.0, 3600),

    LEN500(300.0,500.0, 5300),

    LEN700(500.0,700.0, 7600),

    LEN1000(700.0,1000.0, 12100),

    LEN1400(1000.0,1400.0, 16000),

    LEN1800(1400.0,1800.0, 20000),

    LEN2500(1800.0,2500.0, 23000),

    LEN2500UP(2500.0, Double.MAX_VALUE, 26000),
    ;

    // 0<= T < 3.0
    // 3.0 <= T <6.0
    // 6.0 <= T < 999999

    private final double minLength;
    private final double maxLength;
    private final int cost;

    public static int getCostByBend(double length){
        int cost = 0;

        for(MetalCostByRBend m : MetalCostByRBend.values()){
            if(m.minLength < length && length <= m.maxLength){
                cost = m.cost;
            }
        }

        return cost;
    }
}
