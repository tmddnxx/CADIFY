package com.cadify.cadifyWAS.service.file.enumValues.metal.priceValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalCostByKg {

    /* STEEL */
    SPCC("SPCC", 1992),
    SPHC("SPHC", 1944),
    SECC("SECC", 2064),
    SS400("SS400", 1992),

    /* STAIN */
    SUS304_NO1("SUS304_No1", 8160),
    SUS304_2B("SUS304_2B", 8400),
    SUS304_SH("SUS304_SH", 10800),
    SUS304_SP("SUS304_SP", 8880),
    SUS304_DP("SUS304_DP", 9360),

    /* AL */
    AL5052("AL5052", 13920)
    ;

    private final String material; // 재질
    private final int cost; // 단가


    // kg * cost 반환 (kg당 단가)
    public static Double getCostByKg(String material, Double kg){

        Double cost = null;

        for(MetalCostByKg m : MetalCostByKg.values()){
            if(m.material.equals(material)){
                cost = m.cost * kg;
            }
        }

        return cost == null ? 0 : cost;
    }
}
