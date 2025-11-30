package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.bending;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalRBendByThickness {

    /* STEEL */
    SPCC("SPCC", 3.0),
    SPHC("SPHC", 3.0),
    SECC("SECC", 3.0),
    SS400("SS400", 3.0),

    /* STAIN */
    SUS304_NO_1("SUS304_No1", 3.0),
    SUS304_2B("SUS304_2B", 3.0),
    SUS304_SH("SUS304_SH", 3.0),
    SUS304_SP("SUS304_SP", 3.0),
    SUS304_DP("SUS304_DP", 3.0),

    /* AL */
    AL5052("AL5052", 3.0)
    ;

    private final String material;
    private final double thickness;

    public static Double getThickness(String material){

        for(MetalRBendByThickness m : MetalRBendByThickness.values()){
            if(m.material.equals(material)){
                return m.thickness;
            }
        }

        return null;
    }
}
