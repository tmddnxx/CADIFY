package com.cadify.cadifyWAS.service.file.enumValues.cnc.priceValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CNCCostByHole {

    TAP("TAP", 90)
    ;

    private final String type;
    private final int cost;

    public static int getCostByHole(String type){
        int cost = 0;
            for(CNCCostByHole m : CNCCostByHole.values()){
                if(m.type.equals(type)){
                    cost = m.cost;
                }
            }
        return cost;
    }
}
