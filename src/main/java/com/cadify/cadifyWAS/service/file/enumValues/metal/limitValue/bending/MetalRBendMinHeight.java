package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.bending;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalRBendMinHeight {
    /* 스틸 계열*/
    MATERIAL_STEEL_0_8("steel", 0.8, 3.9),
    MATERIAL_STEEL_1_0("steel", 1.0, 3.5),
    MATERIAL_STEEL_1_2("steel", 1.2, 6.3),
    MATERIAL_STEEL_1_6("steel", 1.6, 5.8),
    MATERIAL_STEEL_2_0("steel", 2.0, 6.5),
    MATERIAL_STEEL_2_3("steel", 2.3, 7.9),
    MATERIAL_STEEL_2_6("steel", 2.6, 12.9),
    MATERIAL_STEEL_3_0("steel", 3.0, 20.5),
    MATERIAL_STEEL_3_2("steel", 3.2, 27.1),

    /* 알류미늄 계열*/
    MATERIAL_AL_0_8("al", 0.8, 3.2),
    MATERIAL_AL_1_0("al", 1.0, 3.5),
    MATERIAL_AL_1_2("al", 1.2, 6.3),
    MATERIAL_AL_1_5("al", 1.5, 6.0),
    MATERIAL_AL_2_0("al", 2.0, 6.4),
    MATERIAL_AL_2_5("al", 2.5, 6.3),
    MATERIAL_AL_3_0("al", 3.0, 7.0),

    /* 스테인레스 계열*/
    MATERIAL_STAIN_0_5("stain", 0.5, 4.8),
    MATERIAL_STAIN_0_8("stain", 0.8, 3.2),
    MATERIAL_STAIN_1_0("stain", 1.0, 3.5),
    MATERIAL_STAIN_1_2("stain", 1.2, 6.3),
    MATERIAL_STAIN_1_5("stain", 1.5, 6.0),
    MATERIAL_STAIN_2_0("stain", 2.0, 6.4),
    MATERIAL_STAIN_2_5("stain", 2.5, 6.3),
    MATERIAL_STAIN_3_0("stain", 3.0, 7.0)
    ;

    private final String material;
    private final double thickness;
    private final double height;

    // 두께와 재질에 따른 최소 높이 판단하기
    public static Double getHeightByMaterial(String material, double thickness) {
        for (MetalRBendMinHeight m : MetalRBendMinHeight.values()) {
            if (m.material.equals(material) && m.thickness == thickness) {
                return m.height;
            }
        }
        return null; // 조건에 맞는 재질과 두께가 없으면 null 반환
    }

}
