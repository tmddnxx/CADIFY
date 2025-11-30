package com.cadify.cadifyWAS.service.file.enumValues.common;

import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum CommonDiff {

    D0("ISO 2768(일반)", "D0", "cnc"), // ISO 2768(일반)
    D1("+-0.10mm", "D1", "cnc"), // +-0.10mm
    D2("+-0.05mm", "D2", "cnc") // +-0.05mm
    ;

    private final String label;
    private final String value;
    private final String type; // metal, cnc, common

    public static List<OptionDTO.OptionType> getCommonDiffList() {
        return Arrays.stream(CommonDiff.values())
                .map(commonDiff -> (OptionDTO.OptionType) new OptionDTO.Options(commonDiff.getLabel(), commonDiff.getValue(), commonDiff.getType()))
                .toList();
    }
}
