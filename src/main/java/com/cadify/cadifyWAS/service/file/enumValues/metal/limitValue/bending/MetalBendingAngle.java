package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.bending;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalBendingAngle {

    THICKNESS_0_8(0.8, 45),
    THICKNESS_1_0(1.0, 45),
    THICKNESS_1_2(1.2, 45),
    THICKNESS_1_5(1.5, 45),
    THICKNESS_1_6(1.6, 45),
    THICKNESS_2_0(2.0, 45),
    THICKNESS_2_3(2.3, 45),
    THICKNESS_2_5(2.5, 45),
    THICKNESS_3_0(3.0, 45),
    THICKNESS_4_0(4.0, 45),
    THICKNESS_5_0(5.0, 45),
    THICKNESS_6_0(6.0, 45),
    ;


    private final double thickness;
    private final double angle;

    // 두께에 따른 최소 예각 한계치 구하기
    public static Double getAngleByThickness(double thickness) {
        for (MetalBendingAngle material : values()) {
            if (material.getThickness() == thickness) {
                return material.getAngle();
            }
        }
        return null;
    }
}
