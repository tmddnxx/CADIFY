package com.cadify.cadifyWAS.service.file.enumValues.metal.priceValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalCostByBend {

    LEN100_UNDER3(0.0,3.0, 0, 100, 300),
    LEN100_UNDER6(3.0, 6.0, 0, 100, 350),
    LEN100_OVER6(6.0, Double.MAX_VALUE, 0, 100, 420),

    LEN300_UNDER3(0.0,3.0, 100, 300, 360),
    LEN300_UNDER6(3.0, 6.0, 100, 300, 460),
    LEN300_OVER6(6.0, Double.MAX_VALUE, 100, 300, 520),

    LEN500_UNDER3(0.0,3.0, 300, 500, 530),
    LEN500_UNDER6(3.0, 6.0, 300, 500, 670),
    LEN500_OVER6(6.0, Double.MAX_VALUE, 300, 500, 720),

    LEN700_UNDER3(0.0,3.0, 500, 700, 760),
    LEN700_UNDER6(3.0, 6.0, 500, 700, 980),
    LEN700_OVER6(6.0, Double.MAX_VALUE, 500, 700, 1070),

    LEN1000_UNDER3(0.0,3.0, 700, 1000, 1210),
    LEN1000_UNDER6(3.0, 6.0, 700, 1000, 1380),
    LEN1000_OVER6(6.0, Double.MAX_VALUE, 700, 1000, 1480),

    LEN1400_UNDER3(0.0,3.0, 1000, 1400, 1600),
    LEN1400_UNDER6(3.0, 6.0, 1000, 1400, 1930),
    LEN1400_OVER6(6.0, Double.MAX_VALUE, 1000, 1400, 2040),

    LEN1800_UNDER3(0.0,3.0, 1400, 1800, 2000),
    LEN1800_UNDER6(3.0, 6.0, 1400, 1800, 2450),
    LEN1800_OVER6(6.0, Double.MAX_VALUE, 1400, 1800, 2580),

    LEN2500_UNDER3(0.0,3.0, 1800, 2500, 2300),
    LEN2500_UNDER6(3.0, 6.0, 1800, 2500, 2900),
    LEN2500_OVER6(6.0, Double.MAX_VALUE, 1800, 2500, 3030),

    LEN2500UP_UNDER3(0.0,3.0, 2500, Double.MAX_VALUE, 2600),
    LEN2500UP_UNDER6(3.0, 6.0, 2500, Double.MAX_VALUE, 3400),
    LEN2500UP_OVER6(6.0, Double.MAX_VALUE, 2500, Double.MAX_VALUE, 3540),
    ;

    // 0<= T < 3.0
    // 3.0 <= T <6.0
    // 6.0 <= T < 999999

    private final double minThickness;
    private final double maxThickness;
    private final double minLength;
    private final double maxLength;
    private final int cost;

    public static int getCostByBend(double thickness, double length){
        int cost = 0;

        for(MetalCostByBend m : MetalCostByBend.values()){
            if(m.minThickness <= thickness && thickness < m.maxThickness && m.minLength < length && length <= m.maxLength){
                cost = m.cost;
            }
        }

        return cost;
    }



}
