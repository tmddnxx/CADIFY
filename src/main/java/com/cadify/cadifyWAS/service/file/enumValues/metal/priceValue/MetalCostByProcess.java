package com.cadify.cadifyWAS.service.file.enumValues.metal.priceValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalCostByProcess {

    /* STEEL */
    SPCC_UNDER_3("SPCC", 0.0,3.1, 1.1),
    SPCC_UNDER_6("SPCC", 3.1,6.0, 1.3),
    SPCC_OVER_6("SPCC", 6.0,Double.MAX_VALUE, 1.8),

    SPHC_UNDER_3("SPHC", 0.0,3.1, 1.1),
    SPHC_UNDER_6("SPHC", 3.1,6.0, 1.3),
    SPHC_OVER_6("SPHC", 6.0,Double.MAX_VALUE, 1.8),

    SECC_UNDER_3("SECC", 0.0,3.1, 1.1),
    SECC_UNDER_6("SECC", 3.1,6.0, 1.3),
    SECC_OVER_6("SECC", 6.0,Double.MAX_VALUE, 1.8),

    SS400_UNDER_3("SS400", 0.0,3.1, 1.1),
    SS400_UNDER_6("SS400", 3.1,6.0, 1.3),
    SS400_OVER_6("SS400", 6.0,Double.MAX_VALUE, 1.8),

    /* STAIN */
    SUS304_NO1_UNDER_3("SUS304_No1", 0.0,3.1, 1.2),
    SUS304_NO1_UNDER_6("SUS304_No1", 3.1,6.0, 1.5),
    SUS304_NO1_OVER_6("SUS304_No1", 6.0,Double.MAX_VALUE, 1.9),

    SUS304_2B_UNDER_3("SUS304_2B", 0.0,3.1, 1.2),
    SUS304_2B_UNDER_6("SUS304_2B", 3.1,6.0, 1.5),
    SUS304_2B_OVER_6("SUS304_2B", 6.0,Double.MAX_VALUE, 1.9),

    SUS304_SH_UNDER_3("SUS304_SH", 0.0,3.1, 1.3),
    SUS304_SH_UNDER_6("SUS304_SH", 3.1,6.0, 1.7),
    SUS304_SH_OVER_6("SUS304_SH", 6.0,Double.MAX_VALUE, 2.0),

    SUS304_SP_UNDER_3("SUS304_SP", 0.0,3.1, 1.3),
    SUS304_SP_UNDER_6("SUS304_SP", 3.1,6.0, 1.7),
    SUS304_SP_OVER_6("SUS304_SP", 6.0,Double.MAX_VALUE, 2.0),

    SUS304_DP_UNDER_3("SUS304_DP", 0.0,3.1, 1.3),
    SUS304_DP_UNDER_6("SUS304_DP", 3.1,6.0, 1.7),
    SUS304_DP_OVER_6("SUS304_DP", 6.0,Double.MAX_VALUE, 2.0),

    /* AL */
    AL5052_UNDER_3("AL5052", 0.0,3.1, 1.3),
    AL5052_UNDER_6("AL5052", 3.1,6.0, 1.7),
    AL5052_OVER_6("AL5052", 6.0,Double.MAX_VALUE, 2.0)

    ;

    private final String material;
    private final double minThickness;
    private final double maxThickness;
    private final double cost;

    public static Double getCostByProcess(String material, double thickness, double totalLength){
        Double cost = null;
        for(MetalCostByProcess m : MetalCostByProcess.values()){
            if(m.material.equals(material)){
                if(m.minThickness <= thickness && thickness < m.maxThickness){
                    cost = m.cost * totalLength;
                }
            }
        }

        return cost == null ? 0 : cost;
    }

}
