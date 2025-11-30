package com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.material;

import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

@Getter
@AllArgsConstructor
public enum MetalMaterialByThickness {

    // SPCC 가능 두께
    SPCC_0_5("SPCC", 0.5),
    SPCC_0_8("SPCC", 0.8),
    SPCC_1_0("SPCC", 1.0),
    SPCC_1_2("SPCC", 1.2),
    SPCC_1_6("SPCC", 1.6),
    SPCC_2_0("SPCC", 2.0),
    SPCC_2_3("SPCC", 2.3),
    SPCC_2_6("SPCC", 2.6),
    SPCC_3_0("SPCC", 3.0),
    SPCC_3_2("SPCC", 3.2),
    SPCC_4_0("SPCC", 4.0),
    SPCC_4_5("SPCC", 4.5),
    SPCC_5_0("SPCC", 5.0),
    SPCC_6_0("SPCC", 6.0),

    // SPHC 가능 두께
    SPHC_2_3("SPHC", 2.3),
    SPHC_2_6("SPHC", 2.6),
    SPHC_3_0("SPHC", 3.0),
    SPHC_3_2("SPHC", 3.2),
    SPHC_4_0("SPHC", 4.0),
    SPHC_4_5("SPHC", 4.5),
    SPHC_5_0("SPHC", 5.0),
    SPHC_6_0("SPHC", 6.0),

    // SS400 가능두께
    SS400_0_8("SS400", 0.8),
    SS400_1_0("SS400", 1.0),
    SS400_1_2("SS400", 1.2),
    SS400_1_6("SS400", 1.6),
    SS400_2_0("SS400", 2.0),
    SS400_2_3("SS400", 2.3),
    SS400_2_6("SS400", 2.6),
    SS400_3_0("SS400", 3.0),
    SS400_3_2("SS400", 3.2),
    SS400_4_0("SS400", 4.0),
    SS400_4_5("SS400", 4.5),
    SS400_5_0("SS400", 5.0),
    SS400_6_0("SS400", 6.0),
    SS400_9_0("SS400", 9.0),
    SS400_12_0("SS400", 12.0),

    // SECC 가능두께
    SECC_0_8("SECC", 0.8),
    SECC_1_0("SECC", 1.0),
    SECC_1_2("SECC", 1.2),
    SECC_1_6("SECC", 1.6),
    SECC_2_0("SECC", 2.0),

    // SUS304_No1 가능두께
    SUS304_NO_3_0("SUS304_No1", 3.0),
    SUS304_NO_4_0("SUS304_No1", 4.0),
    SUS304_NO_5_0("SUS304_No1", 5.0),
    SUS304_NO_6_0("SUS304_No1", 6.0),
    SUS304_NO_8_0("SUS304_No1", 8.0),
    SUS304_NO_9_0("SUS304_No1", 9.0),
    SUS304_NO_10_0("SUS304_No1", 10.0),
    SUS304_NO_12_0("SUS304_No1", 12.0),
    SUS304_NO_15_0("SUS304_No1", 15.0),

    // SUS304_2B 가능두께
    SUS304_2B_0_2("SUS304_2B", 0.2),
    SUS304_2B_0_3("SUS304_2B", 0.3),
    SUS304_2B_0_4("SUS304_2B", 0.4),
    SUS304_2B_0_5("SUS304_2B", 0.5),
    SUS304_2B_0_8("SUS304_2B", 0.8),
    SUS304_2B_1_0("SUS304_2B", 1.0),
    SUS304_2B_1_2("SUS304_2B", 1.2),
    SUS304_2B_1_5("SUS304_2B", 1.5),
    SUS304_2B_2_0("SUS304_2B", 2.0),
    SUS304_2B_2_5("SUS304_2B", 2.5),
    SUS304_2B_3_0("SUS304_2B", 3.0),

    // SUS304_SH
    SUS304_SH_0_8("SUS304_SH", 0.8),
    SUS304_SH_1_0("SUS304_SH", 1.0),
    SUS304_SH_1_2("SUS304_SH", 1.2),
    SUS304_SH_1_5("SUS304_SH", 1.5),
    SUS304_SH_2_0("SUS304_SH", 2.0),
    SUS304_SH_2_5("SUS304_SH", 2.5),
    SUS304_SH_3_0("SUS304_SH", 3.0),

    // SUS304_SP
    SUS304_SP_0_5("SUS304_SP", 0.5),
    SUS304_SP_0_8("SUS304_SP", 0.8),
    SUS304_SP_1_0("SUS304_SP", 1.0),
    SUS304_SP_1_2("SUS304_SP", 1.2),
    SUS304_SP_1_5("SUS304_SP", 1.5),
    SUS304_SP_2_0("SUS304_SP", 2.0),
    SUS304_SP_2_5("SUS304_SP", 2.5),
    SUS304_SP_3_0("SUS304_SP", 3.0),

    // SUS304_DP
    SUS304_DP_0_5("SUS304_DP", 0.5),
    SUS304_DP_0_8("SUS304_DP", 0.8),
    SUS304_DP_1_0("SUS304_DP", 1.0),
    SUS304_DP_1_2("SUS304_DP", 1.2),
    SUS304_DP_1_5("SUS304_DP", 1.5),
    SUS304_DP_2_0("SUS304_DP", 2.0),
    SUS304_DP_2_5("SUS304_DP", 2.5),
    SUS304_DP_3_0("SUS304_DP", 3.0),

    // AL5052 가능두께
    AL5052_0_5("AL5052", 0.5),
    AL5052_0_8("AL5052", 0.8),
    AL5052_1_0("AL5052", 1.0),
    AL5052_1_2("AL5052", 1.2),
    AL5052_1_5("AL5052", 1.5),
    AL5052_2_0("AL5052", 2.0),
    AL5052_2_5("AL5052", 2.5),
    AL5052_3_0("AL5052", 3.0),
    AL5052_4_0("AL5052", 4.0),
    AL5052_5_0("AL5052", 5.0),
    AL5052_6_0("AL5052", 6.0),
    AL5052_8_0("AL5052", 8.0),
    AL5052_10_0("AL5052",10.0 )
    ;

    private final String material;
    private final double thickness;

    public static boolean getMaterialByThickness(String material, double thickness){
        for(MetalMaterialByThickness m : MetalMaterialByThickness.values()){
            if(m.material.equals(material) && m.thickness == thickness){
                return true;
            }
        }
        return false;
    }

    public static List<OptionDTO.OptionType> getAllThicknessListByMaterial() {
        Map<String, List<Double>> map = new HashMap<>();

        for (MetalMaterialByThickness mt : MetalMaterialByThickness.values()) {
            map.computeIfAbsent(mt.getMaterial(), k -> new ArrayList<>()).add(mt.getThickness());
        }

        List<OptionDTO.MaterialByThickness> result = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : map.entrySet()) {
            List<Double> sortedList = new ArrayList<>(entry.getValue());
            Collections.sort(sortedList); // 두께 오름차순 정렬
            result.add(
                    OptionDTO.MaterialByThickness.builder()
                            .material(entry.getKey())
                            .thicknessList(sortedList)
                            .build()
            );
        }

        return new ArrayList<>(result);
    }
}
