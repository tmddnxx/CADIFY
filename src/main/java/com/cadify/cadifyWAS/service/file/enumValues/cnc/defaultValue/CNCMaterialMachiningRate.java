package com.cadify.cadifyWAS.service.file.enumValues.cnc.defaultValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CNCMaterialMachiningRate { // 재질별 가공속도 비율

    SM45C("SM45C", 1.0),                      // 탄소강
    SS400("SS400", 1.0),                      // 구조용 탄소강
    SKD11("SKD11", 2.0),                      // 공구강 (고경도)
    AL6061("AL6061", 1.0),                    // 알루미늄 6061
    AL5052("AL5052", 0.8),                    // 알루미늄 5052
    AL7075("AL7075", 1.0),                    // 알루미늄 7075
    SUS303("SUS303", 1.0),                    // 스테인리스 303
    SUS304("SUS304", 1.5),                    // 스테인리스 304 (상대적으로 고경도)
    SUS316("SUS316", 1.7),                    // 스테인리스 316 (고경도, 내식성 우수)
    BRASS_C3604("BRASS_C3604", 1.0),          // 황동 (C3604, 가공성 우수)
    POM_WHITE("POM_WHITE", 0.8),              // 폴리아세탈 (흰색, 플라스틱 소재)
    POM_BLACK("POM_BLACK", 0.8),              // 폴리아세탈 (검정)
    MC_NYLON_BLUE("MC_NYLON_BLUE", 0.8),      // MC 나일론 (청색)
    MC_NYLON_IVORY("MC_NYLON_IVORY", 0.8),    // MC 나일론 (아이보리)
    PEEK("PEEK", 0.8);                        // PEEK (회갈색, 고성능 플라스틱)

    private final String material; // 재질
    private final double rate; // 가공속도 비율

    public static Double getMachiningRate(String material) { // 재질에 따른 가공속도 비율
        for (CNCMaterialMachiningRate m : values()) {
            if (m.material.equals(material)) {
                return m.rate;
            }
        }
        return null;
    }
}
