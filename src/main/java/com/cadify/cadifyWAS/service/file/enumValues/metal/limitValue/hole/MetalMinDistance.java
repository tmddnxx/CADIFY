package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.hole;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MetalMinDistance {


    /* 스틸 계열*/
    MATERIAL_STEEL_0_6("steel", 0.6, 4.3, 2.2),
    MATERIAL_STEEL_0_8("steel", 0.8, 4.3, 2.2),
    MATERIAL_STEEL_1_0("steel", 1.0, 4.3, 3.2),
    MATERIAL_STEEL_1_2("steel", 1.2, 5.7, 3.7),
    MATERIAL_STEEL_1_6("steel", 1.6, 7.1, 3.7),
    MATERIAL_STEEL_2_0("steel", 2.0, 8.5, 6.0),
    MATERIAL_STEEL_2_3("steel", 2.3, 9.9, 7.2),
    MATERIAL_STEEL_2_6("steel", 2.6, 11.3, 9.5),
    MATERIAL_STEEL_3_0("steel", 3.0, 16.7, 11.5),
    MATERIAL_STEEL_3_2("steel", 3.2, 17.7, 12.5),
    MATERIAL_STEEL_4_0("steel", 4.0, 28.3, 23.2),
    MATERIAL_STEEL_4_5("steel", 4.5, 28.3, 23.2),
    MATERIAL_STEEL_6_0("steel", 6.0, 35.4, 26.7),

    /* 알류미늄 계열*/
    MATERIAL_AL_0_8("al", 0.8, 4.3, 2.2),
    MATERIAL_AL_1_0("al", 1.0, 4.3, 3.2),
    MATERIAL_AL_1_2("al", 1.2, 5.7, 3.7),
    MATERIAL_AL_1_5("al", 1.5, 6.9, 3.7),
    MATERIAL_AL_2_0("al", 2.0, 8.5, 6.0),
    MATERIAL_AL_2_5("al", 2.5, 11.3, 6.3),
    MATERIAL_AL_3_0("al", 3.0, 16.7, 11.5),
    MATERIAL_AL_4_0("al", 4.0, 26.1, 20.3),

    /* 스테인레스 계열*/
    MATERIAL_STAIN_0_5("stain", 0.5, 4.3, 2.2),
    MATERIAL_STAIN_0_8("stain", 0.8, 4.3, 2.2),
    MATERIAL_STAIN_1_0("stain", 1.0, 4.3, 3.2),
    MATERIAL_STAIN_1_2("stain", 1.2, 5.7, 3.7),
    MATERIAL_STAIN_1_5("stain", 1.5, 6.9, 3.7),
    MATERIAL_STAIN_2_0("stain", 2.0, 8.5, 6.0),
    MATERIAL_STAIN_2_5("stain", 2.5, 11.3, 6.3),
    MATERIAL_STAIN_3_0("stain", 3.0, 16.7, 11.5),
    MATERIAL_STAIN_4_0("stain", 4.0, 26.1, 20.3),
    MATERIAL_STAIN_5_0("stain", 5.0, 30.4, 24.1)
    ;

    private final String material;
    private final double thickness;
    private final double guarantee; // 보증치 (권장)
    private final double minDistance; // 최소치


    // 두께와 재질에 따른 보증치 가져오기
    public static Double getGuaranteeByMaterial(String material, double thickness) {
        for (MetalMinDistance m : MetalMinDistance.values()) {
            if (m.material.equals(material) && m.thickness == thickness) {
                return m.guarantee;
            }
        }
        return null; // 조건에 맞는 재질과 두께가 없으면 null 반환
    }

    // 두께와 재질에 따른 최소거리 가져오기
    public static Double getMinDistanceByMaterial(String material, double thickness) {
        for (MetalMinDistance m : MetalMinDistance.values()) {
            if (m.material.equals(material) && m.thickness == thickness) {
                return m.minDistance;
            }
        }
        return null; // 조건에 맞는 재질과 두께가 없으면 null 반환
    }
}
