package com.cadify.cadifyWAS.service.file.enumValues.cnc.priceValue;

import com.cadify.cadifyWAS.service.file.enumValues.metal.priceValue.MetalCostByKg;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CNCCostByKg {

    SM45C("SM45C", 2400),               // SM45C (탄소강)
    SS400("SS400", 2000),               // SS400 (구조용 탄소강)
    SKD11("SKD11", 6000),               // SKD11 (공구강)
    AL6061("AL6061", 8000),             // 알루미늄 6061
    AL5052("AL5052", 7000),             // 알루미늄 5052
    AL7075("AL7075", 12000),            // 알루미늄 7075
    SUS303("SUS303", 3000),             // 스테인리스 303
    SUS304("SUS304", 2800),             // 스테인리스 304
    SUS316("SUS316", 4300),             // 스테인리스 316
    BRASS_C3604("BRASS_C3604", 16000),        // 황동 C3604
    POM_WHITE("POM_WHITE", 10000),      // 폴리아세탈(흰색)
    POM_BLACK("POM_BLACK", 10000),      // 폴리아세탈(검정)
    MC_NYLON_BLUE("MC_NYLON_BLUE", 9000),     // MC 나일론(청색)
    MC_NYLON_IVORY("MC_NYLON_IVORY", 9000),   // MC 나일론(아이보리)
    PEEK_BROWN("PEEK", 60000);          // PEEK (회갈색)

    private final String material; // 재질
    private final int cost; // 단가

    // kg * cost 반환 (kg당 단가)
    public static Double getCostByKg(String material, Double kg){

        Double cost = null;

        for(CNCCostByKg m : CNCCostByKg.values()){
            if(m.material.equals(material)){
                cost = m.cost * kg;
            }
        }

        return cost == null ? 0 : cost;
    }
}
