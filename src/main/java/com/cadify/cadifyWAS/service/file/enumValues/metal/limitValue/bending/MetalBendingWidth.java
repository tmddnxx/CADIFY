package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.bending;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalBendingWidth {
    THICKNESS_0_2(0.2, 10, 200),
    THICKNESS_0_3(0.3, 5,1200),
    THICKNESS_0_4(0.4, 5,1200),
    THICKNESS_0_5(0.5, 5,1200),
    THICKNESS_0_8(0.8, 5,1200),
    THICKNESS_1_0(1.0, 5,1200),
    THICKNESS_1_2(1.2, 5,1200),
    THICKNESS_1_5(1.5, 5,1200),
    THICKNESS_1_6(1.6, 10,1200),
    THICKNESS_2_0(2.0, 5,1200),
    THICKNESS_2_3(2.3, 10,1200),
    THICKNESS_2_5(2.5, 5,1200),
    THICKNESS_2_6(2.6, 10,1200),
    THICKNESS_3_0(3.0, 5,1200),
    THICKNESS_3_2(3.2, 10,1200),
    THICKNESS_4_0(4.0, 5,1200),
    THICKNESS_4_5(4.5, 10,1200),
    THICKNESS_5_0(5.0, 5,1200)
    ;


    private final double thickness;
    private final double minGrossLength;
    private final double maxGrossLength;

    // 두께에 따른 최소 폭 한계치 구하기
    public static Double getMinLengthByThickness(double thickness) {
        for (MetalBendingWidth material : values()) {
            if (material.getThickness() == thickness) {
                return material.getMinGrossLength();
            }
        }
        return null;
    }

    // 두께에 따른 최대 폭 한계치 구하기
    public static Double getMaxLengthByThickness(double thickness) {
        for (MetalBendingWidth material : values()) {
            if (material.getThickness() == thickness) {
                return material.getMaxGrossLength();
            }
        }
        return null;
    }

}
