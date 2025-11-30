package com.cadify.cadifyWAS.service.file.enumValues.metal.priceValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalCostByHole {

    TAP("TAP", 90),
    CS("CS", 90)
    ;

    private final String type;
    private final int cost;

    public static int getCostByHole(String type){
        int cost = 0;
            for(MetalCostByHole m : MetalCostByHole.values()){
                if(m.type.equals(type)){
                    cost = m.cost;
                }
            }
        return cost;
    }

}
