package com.cadify.cadifyWAS.service.file.enumValues.metal.defaultValue;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum MetalHoleSize {

    M2("M2", 1.6),
    M2_5("M2.5", 2.05),
    M3("M3", 2.5),
    M3_T("M3", 2.53),
    M4("M4", 3.3),
    M4_T("M4", 3.33),
    M5("M5", 4.2),
    M5_T("M5", 4.23),
    M6("M6", 5.0),
    M6_T("M6", 5.04),
    M8("M8", 6.75),
    M8_T("M8", 6.78),
    M10("M10", 8.5),
    M10_T("M10", 8.53),
    M12("M12", 10.25),
    M12_T("M12", 10.27),
    M14("M14", 12.0),
    M14_T("M14", 12.02),
    M16_T("M14", 14.02);


    private final String type;
    private final double diameter;

    public static String getTypeByHoleSize(double diameter) {
        for (MetalHoleSize holeSize : MetalHoleSize.values()) {
            if (holeSize.getDiameter() == diameter) {
                return holeSize.getType();
            }
        }
        return "hole";
    }

}
