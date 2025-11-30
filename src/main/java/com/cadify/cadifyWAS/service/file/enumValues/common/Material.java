package com.cadify.cadifyWAS.service.file.enumValues.common;

import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum Material {

    // 판금
    SPCC("SPCC", "SPCC", "sheet_metal"),
    SPHC("SPHC", "SPHC", "sheet_metal"),
    SECC("SECC", "SECC", "sheet_metal"),
    SUS304_No1("SUS304 No.1", "SUS304_No1", "sheet_metal"),
    SUS304_2B("SUS304 2B", "SUS304_2B", "sheet_metal"),
    SUS304_SH("SUS304 단면 헤어라인", "SUS304_SH", "sheet_metal"),
    SUS304_SP("SUS304 단면 폴리싱", "SUS304_SP", "sheet_metal"),
    SUS304_DP("SUS304 양면 폴리싱", "SUS304_DP", "sheet_metal"),


    // 절삭
    SM45C("SM45C", "SM45C", "cnc"), // SM45C (탄소강)
    SKD11("SKD11", "SKD11", "cnc"), // SKD11 (공구강)

    AL6061("AL6061", "AL6061", "cnc"), // 알루미늄 6061
    AL7075("AL7075", "AL7075", "cnc"), // 알루미늄 7075

    SUS303("SUS303", "SUS303", "cnc"), // 스테인리스 303
    SUS304("SUS304", "SUS304", "cnc"), // 스테인리스 304
    SUS316("SUS316", "SUS316", "cnc"), // 스테인리스 316

    BRASS_C3604("황동(C3604)", "BRASS_C3604", "cnc"), // 황동 (C3604)

    POM_WHITE("폴리아세탈(흰색)", "POM_WHITE", "cnc"), // 폴리아세탈 (흰색)
    POM_BLACK("폴리아세탈(검정)", "POM_BLACK", "cnc"), // 폴리아세탈 (검정)
    MC_NYLON_BLUE("MC 나일론(청색)", "MC_NYLON_BLUE", "cnc"), // MC 나일론 (청색)
    MC_NYLON_IVORY("MC 나일론(아이보리)", "MC_NYLON_IVORY", "cnc"), // MC 나일론 (아이보리)

    PEEK("PEEK(회갈색)", "PEEK", "cnc"), // PEEK (회갈색)

    // 공통
    AL5052("AL5052", "AL5052", "common"),
    SS400("SS400", "SS400", "common"),
    ;
    

    private final String label;
    private final String value;
    private final String type;

    public static List<OptionDTO.OptionType> getMaterialList() {
        return Arrays.stream(Material.values())
                .map(material -> (OptionDTO.OptionType) new OptionDTO.Options(material.getLabel(), material.getValue(), material.getType()))
                .toList();
    }

}
