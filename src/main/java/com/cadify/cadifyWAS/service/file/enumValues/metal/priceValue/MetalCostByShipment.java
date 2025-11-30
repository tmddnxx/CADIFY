package com.cadify.cadifyWAS.service.file.enumValues.metal.priceValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalCostByShipment {

    FLAT("FLAT", 1000),
    FOLDED("FOLDED", 3500),
    RFOLDED("RFOLDED", 5000)
    ;

    private final String type; // metal 타입 (flat or folded)
    private final int cost; // 1일째일때 더해지는 단가

    public static int getCostByType(String type){
        int cost = 0;
        for(MetalCostByShipment m : MetalCostByShipment.values()){
            if(m.type.equals(type)){
                cost = m.cost;
            }
        }

        return cost;
    }


}
