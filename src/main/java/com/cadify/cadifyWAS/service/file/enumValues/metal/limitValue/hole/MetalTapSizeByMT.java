package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.hole;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public enum MetalTapSizeByMT {

    /* -------------------------- 스틸 ------------------------------------ */
    // SECC는 0.8T~2.0T까지만 처리
    SECC_0_8("SECC", 0.8, List.of("M3", "M4", "M5", "M6")),
    SECC_1_0("SECC", 1.0, List.of("M3", "M4", "M5", "M6", "M8")),
    SECC_1_2("SECC", 1.2, List.of("M3", "M4", "M5", "M6", "M8")),
    SECC_1_6("SECC", 1.6, List.of("M3", "M4", "M5", "M6", "M8")),
    SECC_2_0("SECC", 2.0, List.of("M3", "M4", "M5", "M6", "M8")),

    // steel 계열은 모두 "STEEL"로 통일
    STEEL_0_8("STEEL", 0.8, List.of("M2", "M3", "M4", "M5", "M6")),
    STEEL_1_0("STEEL", 1.0, List.of("M2", "M3", "M4", "M5", "M6", "M8")),
    STEEL_1_2("STEEL", 1.2, List.of("M2", "M3", "M4", "M5", "M6", "M8")),
    STEEL_1_6("STEEL", 1.6, List.of("M2", "M3", "M4", "M5", "M6", "M8")),
    STEEL_2_0("STEEL", 2.0, List.of("M2", "M3", "M4", "M5", "M6", "M8")),
    STEEL_2_3("STEEL", 2.3, List.of("M3", "M4", "M5", "M6", "M8")),
    STEEL_2_6("STEEL", 2.6, List.of("M3", "M4", "M5", "M6", "M8")),
    STEEL_3_0("STEEL", 3.0, List.of("M3", "M4", "M5", "M6", "M8")),
    STEEL_3_2("STEEL", 3.2, List.of("M3", "M4", "M5", "M6", "M8")),
    STEEL_4_0("STEEL", 4.0, List.of("M4", "M5", "M6", "M8")),
    STEEL_4_5("STEEL", 4.5, List.of("M5", "M6", "M8")),
    STEEL_5_0("STEEL", 5.0, List.of("M5", "M6", "M8", "M10")),
    STEEL_6_0("STEEL", 6.0, List.of("M6", "M8", "M10")),
    STEEL_9_0("STEEL", 9.0, List.of("M8", "M10", "M12", "M14")),
    STEEL_12_0("STEEL", 12.0, List.of("M8", "M10", "M12", "M14")),

    /* -------------------------- 스테인레스 ------------------------------------ */
    // SUS304_No1
    SUS304_NO1_1_0("SUS304_No1", 1.0, List.of("M2", "M2.5", "M3", "M4")),
    SUS304_NO1_1_2("SUS304_No1", 1.2, List.of("M2", "M2.5", "M3", "M4", "M5")),
    SUS304_NO1_1_5("SUS304_No1", 1.5, List.of("M2", "M2.5", "M3", "M4", "M5", "M6")),
    SUS304_NO1_2_0("SUS304_No1", 2.0, List.of("M2", "M2.5", "M3", "M4", "M5", "M6")),
    SUS304_NO1_2_5("SUS304_No1", 2.5, List.of("M3", "M4", "M5", "M6", "M8")),
    SUS304_NO1_3_0("SUS304_No1", 3.0, List.of("M3", "M4", "M5", "M6", "M8")),
    SUS304_NO1_4_0("SUS304_No1", 4.0, List.of("M4", "M5", "M6", "M8")),
    SUS304_NO1_5_0("SUS304_No1", 5.0, List.of("M5", "M6", "M8")),
    SUS304_NO1_6_0("SUS304_No1", 6.0, List.of("M6", "M8", "M10", "M12")),
    SUS304_NO1_8_0("SUS304_No1", 8.0, List.of("M8", "M10", "M12", "M14")),
    SUS304_NO1_10_0("SUS304_No1", 10.0, List.of("M10", "M12", "M14", "M16", "M18")),
    SUS304_NO1_12_0("SUS304_No1", 12.0, List.of("M10", "M12", "M14", "M16", "M18")),
    SUS304_NO1_15_0("SUS304_No1", 15.0, List.of("M10", "M12", "M14", "M16", "M18")),

    // SUS304_2B
    SUS304_2B_1_0("SUS304_2B", 1.0, List.of("M2", "M2.5", "M3", "M4")),
    SUS304_2B_1_2("SUS304_2B", 1.2, List.of("M2", "M2.5", "M3", "M4", "M5")),
    SUS304_2B_1_5("SUS304_2B", 1.5, List.of("M2", "M2.5", "M3", "M4", "M5", "M6")),
    SUS304_2B_2_0("SUS304_2B", 2.0, List.of("M2", "M2.5", "M3", "M4", "M5", "M6")),
    SUS304_2B_2_5("SUS304_2B", 2.5, List.of("M3", "M4", "M5", "M6", "M8")),
    SUS304_2B_3_0("SUS304_2B", 3.0, List.of("M3", "M4", "M5", "M6", "M8")),

    // SUS304 단면/양면 (헤어라인 & 폴리싱) - 3T까지만 처리
    SUS304_POLISH_1_0("SUS304_POLISH", 1.0, List.of("M2", "M2.5", "M3", "M4")),
    SUS304_POLISH_1_2("SUS304_POLISH", 1.2, List.of("M2", "M2.5", "M3", "M4", "M5")),
    SUS304_POLISH_1_5("SUS304_POLISH", 1.5, List.of("M2", "M2.5", "M3", "M4", "M5", "M6")),
    SUS304_POLISH_2_0("SUS304_POLISH", 2.0, List.of("M2", "M2.5", "M3", "M4", "M5", "M6")),
    SUS304_POLISH_2_5("SUS304_POLISH", 2.5, List.of("M3", "M4", "M5", "M6", "M8")),
    SUS304_POLISH_3_0("SUS304_POLISH", 3.0, List.of("M3", "M4", "M5", "M6", "M8")),

    /* -------------------------- 알류미늄 ------------------------------------ */
    AL5052_1_5("AL5052", 1.5, List.of("M3")),
    AL5052_2_0("AL5052", 2.0, List.of("M3", "M4")),
    AL5052_2_5("AL5052", 2.5, List.of("M3", "M4", "M5")),
    AL5052_3_0("AL5052", 3.0, List.of("M3", "M4", "M5")),
    AL5052_4_0("AL5052", 4.0, List.of("M4", "M5", "M6")),
    AL5052_5_0("AL5052", 5.0, List.of("M5", "M6", "M8", "M10")),
    AL5052_6_0("AL5052", 6.0, List.of("M6", "M8", "M10", "M12")),
    AL5052_8_0("AL5052", 8.0, List.of("M6", "M8", "M10", "M12", "M14")),
    AL5052_10_0("AL5052", 10.0, List.of("M8", "M10", "M12", "M14", "M16"))
    ;


    private final String material;
    private final double thickness;
    private final List<String> mType;

    public static boolean isMTypeSupported(String material, double thickness, String mType) {
        for (MetalTapSizeByMT tapSize : MetalTapSizeByMT.values()) {
            // 재질과 두께가 일치하는지 확인
            if (tapSize.getMaterial().equals(material) && tapSize.getThickness() == thickness) {
                // 해당 재질과 두께에 주어진 M타입이 있는지 확인
                if (tapSize.getMType().contains(mType)) {
                    return true;
                }
            }
        }
        return false; // 해당 조건에 맞는 M타입이 없으면 false
    }
}
