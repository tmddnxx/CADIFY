package com.cadify.cadifyWAS.service.file.enumValues.common;

import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum Roughness {

    R0("Ra3.2", "R0", "cnc"),
    R1("Ra1.6", "R1", "cnc");

    private final String label;
    private final String value;
    private final String type;

    public static List<OptionDTO.OptionType> getRoughnessList() {

        return Arrays.stream(Roughness.values())
                .map(roughness -> (OptionDTO.OptionType) new OptionDTO.Options(roughness.getLabel(), roughness.getValue(), roughness.getType()))
                .toList();
    }
}
