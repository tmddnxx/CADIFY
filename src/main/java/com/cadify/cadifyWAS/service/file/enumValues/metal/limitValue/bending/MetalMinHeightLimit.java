package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.bending;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalMinHeightLimit {

    THICKNESS_0_2(0.2, 1.1),
    THICKNESS_0_3(0.3, 1.4),
    THICKNESS_0_4(0.4, 1.4),
    THICKNESS_0_5(0.5, 2.1),
    THICKNESS_0_6(0.6, 2.1),
    THICKNESS_0_7(0.7, 2.1),
    THICKNESS_0_8(0.8, 2.8),
    THICKNESS_1_0(1.0, 4.2),
    THICKNESS_1_2(1.2, 4.9),
    THICKNESS_1_4(1.4, 4.9),
    THICKNESS_1_5(1.5, 7.1),
    THICKNESS_1_6(1.6, 7.1),
    THICKNESS_1_8(1.8, 7.1),
    THICKNESS_2_0(2.0, 8.5),
    THICKNESS_2_3(2.3, 8.5),
    THICKNESS_2_5(2.5, 8.5),
    THICKNESS_3_0(3.0, 12.0),
    THICKNESS_4_0(4.0, 14.1),
    THICKNESS_5_0(5.0, 14.1),
    THICKNESS_6_0(6.0, 14.1),
    THICKNESS_8_0(8.0, 14.1)
    ;

    private final double thickness;
    private final double height;
    
    // 두께에 따른 최소 높이 한계치 구하기
    public static Double getHeightByThickness(double thickness) {
        for (MetalMinHeightLimit material : values()) {
            if (material.getThickness() == thickness) {
                return material.getHeight();
            }
        }
        return null; 
    }
}
