package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.material;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalRBendSizeByMaterial {

    /* STEEL */
    SPCC("SPCC", 10, 10, 1200, 1200),
    SPHC("SPHC", 10, 10, 1200, 1200),
    SECC("SECC", 10, 10, 1200, 1200),
    SS400("SS400", 10, 10, 1200, 1200),

    /* STAIN */
    SUS304_NO_1("SUS304_No1", 10, 10, 1200, 1200),
    SUS304_2B("SUS304_2B", 10, 10, 1200, 1200),
    SUS304_SH("SUS304_SH",  10, 10, 1200, 1200),
    SUS304_SP("SUS304_SP", 10, 10, 1200, 1200),
    SUS304_DP("SUS304_DP", 10, 10, 1200, 1200),

    /* AL */
    AL5052("AL5052", 10, 10, 1200, 1200)
    ;

    private final String material;
    private final int minLength;
    private final int minWidth;
    private final int maxLength;
    private final int maxWidth;

    public static Integer[] getSize(String material){
        Integer[] values = new Integer[4];

        boolean flag = false;
        for(MetalRBendSizeByMaterial m : MetalRBendSizeByMaterial.values()){
            if(m.material.equals(material)){
                flag = true;
                values[0] = m.minLength;
                values[1] = m.minWidth;
                values[2] = m.maxLength;
                values[3] = m.maxWidth;
            }
        }

        return flag ? values : null;
    }
}
