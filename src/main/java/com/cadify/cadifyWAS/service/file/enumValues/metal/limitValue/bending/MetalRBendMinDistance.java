package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.bending;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MetalRBendMinDistance {

    /* 스틸 계열*/
    MATERIAL_STEEL_0_5("steel", 0.5, 2.4, 1.2),
    MATERIAL_STEEL_0_8("steel", 0.8, 4.3, 1.5),
    MATERIAL_STEEL_1_0("steel", 1.0, 4.3, 1.5),
    MATERIAL_STEEL_1_2("steel", 1.2, 5.7, 1.5),
    MATERIAL_STEEL_1_5("steel", 1.5, 7.0, 1.5),
    MATERIAL_STEEL_2_0("steel", 2.0, 8.5, 2.0),
    MATERIAL_STEEL_2_5("steel", 2.5, 11.3, 2.5), // 보증치 판단 필요
    MATERIAL_STEEL_3_0("steel", 3.0, 9.9, 2.5),
    MATERIAL_STEEL_4_0("steel", 4.0, 17.7, 4.0),
    MATERIAL_STEEL_5_0("steel", 5.0, 26.1, 4.0),
    MATERIAL_STEEL_6_0("steel", 6.0, 26.1, 4.0),
    MATERIAL_STEEL_8_0("steel", 8.0, 26.1, 4.0),
    MATERIAL_STEEL_10_0("steel", 10.0, 26.1, 4.0),

    /* 알류미늄 계열*/
    MATERIAL_AL_0_5("al", 0.5, 2.4, 1.2),
    MATERIAL_AL_0_8("al", 0.8, 4.3, 1.5),
    MATERIAL_AL_1_0("al", 1.0, 4.3, 1.5),
    MATERIAL_AL_1_2("al", 1.2, 5.7, 1.5),
    MATERIAL_AL_1_5("al", 1.5, 7.0, 1.5),
    MATERIAL_AL_2_0("al", 2.0, 8.5, 2.0),
    MATERIAL_AL_2_5("al", 2.5, 11.3, 2.5), // 보증치 판단 필요
    MATERIAL_AL_3_0("al", 3.0, 9.9, 2.5),
    MATERIAL_AL_4_0("al", 4.0, 17.7, 4.0),
    MATERIAL_AL_5_0("al", 5.0, 26.1, 4.0),
    MATERIAL_AL_6_0("al", 6.0, 26.1, 4.0),
    MATERIAL_AL_8_0("al", 8.0, 26.1, 4.0),
    MATERIAL_AL_10_0("al", 10.0, 26.1, 4.0),

    /* 스테인레스 계열*/
    MATERIAL_STAIN_0_2("stain", 0.2, 2.4, 1.0),
    MATERIAL_STAIN_0_3("stain", 0.3, 2.4, 1.0),
    MATERIAL_STAIN_0_4("stain", 0.4, 2.4, 1.0),
    MATERIAL_STAIN_0_5("stain", 0.5, 2.4, 1.0),
    MATERIAL_STAIN_0_8("stain", 0.8, 2.7, 1.0),
    MATERIAL_STAIN_1_0("stain", 1.0, 3.9, 1.5),
    MATERIAL_STAIN_1_2("stain", 1.2, 4.0, 1.5),
    MATERIAL_STAIN_1_5("stain", 1.5, 5.3, 1.5),
    MATERIAL_STAIN_2_0("stain", 2.0, 7.8, 2.0),
    MATERIAL_STAIN_2_5("stain", 2.5, 8.2, 2.5),
    MATERIAL_STAIN_3_0("stain", 3.0, 9.0, 2.5),
    MATERIAL_STAIN_4_0("stain", 4.0, 12.0, 3.0),
    MATERIAL_STAIN_5_0("stain", 5.0, 12.0, 3.0),
    MATERIAL_STAIN_6_0("stain", 6.0, 15.0, 4.0),
    MATERIAL_STAIN_8_0("stain", 8.0, 15.0, 4.0),
    MATERIAL_STAIN_10_0("stain", 10.0, 20.0, 5.0),
    MATERIAL_STAIN_12_0("stain", 12.0, 22.0, 5.0),
    MATERIAL_STAIN_15_0("stain", 15.0, 25.0, 6.0),
    ;

    private final String material;
    private final double thickness;
    private final double guarantee; // 보증치 (권장)
    private final double minDistance; // 최소치


    // 두께와 재질에 따른 보증치 가져오기
    public static Double getGuaranteeByMaterial(String material, double thickness) {
        for (MetalRBendMinDistance m : MetalRBendMinDistance.values()) {
            if (m.material.equals(material) && m.thickness == thickness) {
                return m.guarantee;
            }
        }
        return null; // 조건에 맞는 재질과 두께가 없으면 null 반환
    }

    // 두께와 재질에 따른 최소거리 가져오기
    public static Double getMinDistanceByMaterial(String material, double thickness) {
        for (MetalRBendMinDistance m : MetalRBendMinDistance.values()) {
            if (m.material.equals(material) && m.thickness == thickness) {
                return m.minDistance;
            }
        }
        return null; // 조건에 맞는 재질과 두께가 없으면 null 반환
    }
}
