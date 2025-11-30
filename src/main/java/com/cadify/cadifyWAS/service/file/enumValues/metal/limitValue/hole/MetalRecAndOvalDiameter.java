package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.hole;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalRecAndOvalDiameter {

    THICKNESS_0_2(0.2, 0.5),
    THICKNESS_0_3(0.3, 0.5),
    THICKNESS_0_4(0.4, 0.5),
    THICKNESS_0_5(0.5, 0.5),
    THICKNESS_0_8(0.8, 0.8),
    THICKNESS_1_0(1.0, 1.0),
    THICKNESS_1_2(1.2, 1.2),
    THICKNESS_1_5(1.5, 1.5),
    THICKNESS_1_6(1.6, 2.0),
    THICKNESS_2_0(2.0, 2.0),
    THICKNESS_2_3(2.3, 2.3),
    THICKNESS_2_5(2.5, 2.5),
    THICKNESS_2_6(2.6, 2.5),
    THICKNESS_3_0(3.0, 3.0),
    THICKNESS_3_2(3.2, 3.0),
    THICKNESS_4_0(4.0, 4.0),
    THICKNESS_4_5(4.5, 4.0),
    THICKNESS_5_0(5.0, 5.0),
    THICKNESS_6_0(6.0, 6.0),
    THICKNESS_8_0(8.0, 8.0),
    THICKNESS_10_0(10.0, 10.0),
    THICKNESS_12_0(12.0, 10.0),
    THICKNESS_15_0(15.0, 10.0),
    ;


    private final double thickness;
    private final double diameter;

    // 두께에 따른 최소 높이 한계치 구하기
    public static Double getDiameterByThickness(double thickness) {
        for (MetalRecAndOvalDiameter material : values()) {
            if (material.getThickness() == thickness) {
                return material.getDiameter();
            }
        }
        return null;
    }

}
