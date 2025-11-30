package com.cadify.cadifyWAS.service.file.enumValues.cnc.defaultValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CNCMaterialDensity { // 밀도

    SM45C("SM45C", 7.85),               // SM45C (탄소강)
    SS400("SS400", 7.85),               // SS400 (구조용 탄소강)
    SKD11("SKD11", 7.8),                // SKD11 (공구강)
    AL6061("AL6061", 2.70),             // 알루미늄 6061
    AL5052("AL5052", 2.68),             // 알루미늄 5052
    AL7075("AL7075", 2.81),             // 알루미늄 7075
    SUS303("SUS303", 7.98),             // 스테인리스 303
    SUS304("SUS304", 7.93),             // 스테인리스 304
    SUS316("SUS316", 7.98),             // 스테인리스 316
    BRASS_C3604("BRASS_C3604", 8.5),    // 황동 (C3604)
    POM_WHITE("POM_WHITE", 1.41),       // 폴리아세탈 (흰색)
    POM_BLACK("POM_BLACK", 1.41),       // 폴리아세탈 (검정)
    MC_NYLON_BLUE("MC_NYLON_BLUE", 1.15),      // MC 나일론 (청색)
    MC_NYLON_IVORY("MC_NYLON_IVORY", 1.15),    // MC 나일론 (아이보리)
    PEEK("PEEK", 1.30);                 // PEEK (회갈색)

    private final String material; // 재질
    private final double density; // 밀도

    public static Double calculateWeight(String material, double volumeMm3) { // 밀도에 따른 무게 계산
        for (CNCMaterialDensity md : values()) {
            if (md.material.equals(material)) {
                return (volumeMm3 * md.density) / 1000000 ; // kg 계산
            }
        }
        return null;
    }
}
