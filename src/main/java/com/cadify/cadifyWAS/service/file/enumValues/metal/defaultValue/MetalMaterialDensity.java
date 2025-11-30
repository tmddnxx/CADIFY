package com.cadify.cadifyWAS.service.file.enumValues.metal.defaultValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalMaterialDensity { // 밀도

    STEEL("steel",7.85), // 비중
    SUS304("stain",7.93),
    AL5052("al",2.7);
    
    private final String material; // 재질
    private final double density; // 밀도

    public static Double calculateWeight(String material, double volumeMm3) { // 밀도에 따른 무게 계산
        for (MetalMaterialDensity md : values()) {
            if (md.material.equals(material)) {
                return (volumeMm3 * md.density) / 1000000 ; // kg 계산
            }
        }
        return null;
    }
}
