package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.bending;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalRBendingJudge {
    
    /* 스틸 계열*/
    MATERIAL_STEEL_0_8("steel", 10, 150),

    /* 알류미늄 계열*/
    MATERIAL_AL_0_8("al", 10, 150),
    
    /* 스테인레스 계열*/
    MATERIAL_STAIN_0_5("stain", 15, 190),
    ;

    private final String material;
    private final double minRadius;
    private final double maxRadius;

    // 두께와 재질에 따른 R절곡 최소 R값 판단하기
    public static Double getMinRadiusByMaterial(String material) {
        for (MetalRBendingJudge m : MetalRBendingJudge.values()) {
            if (m.material.equals(material)) {
                return m.minRadius;
            }
        }
        return null; // 조건에 맞는 재질과 두께가 없으면 null 반환
    }

    // 두께와 재질에 따른 R절곡 최대 R값 판단하기
    public static Double getMaxRadiusByMaterial(String material) {
        for (MetalRBendingJudge m : MetalRBendingJudge.values()) {
            if (m.material.equals(material)) {
                return m.maxRadius;
            }
        }
        return null; // 조건에 맞는 재질이 없으면 null 반환
    }
}
