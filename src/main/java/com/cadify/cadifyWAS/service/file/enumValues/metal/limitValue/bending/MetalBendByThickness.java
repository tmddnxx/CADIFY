package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.bending;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalBendByThickness {

    /* STEEL */
    SPCC("SPCC", 6.0),
    SPHC("SPHC", 6.0),
    SECC("SECC", 6.0),
    SS400("SS400", 6.0),

    /* STAIN */
    SUS304_NO_1("SUS304_No1", 6.0),
    SUS304_2B("SUS304_2B", 6.0),
    SUS304_SH("SUS304_SH", 6.0),
    SUS304_SP("SUS304_SP", 6.0),
    SUS304_DP("SUS304_DP", 6.0),

    /* AL */
    AL5052("AL5052", 6.0)
    ;

    private final String material;
    private final double thickness;

    public static Double getThickness(String material){

        for(MetalBendByThickness m : MetalBendByThickness.values()){
            if(m.material.equals(material)){
                return m.thickness;
            }
        }

        return null;
    }
}
