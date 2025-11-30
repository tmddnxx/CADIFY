package com.cadify.cadifyWAS.service.file.enumValues.common;

import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum Surface {

    NA("없음","NA", "common"), // 없음
    PWC("분체도장","PWC", "common"), // 분체도장
    BLO("흑착색(사삼회철 피막)","BLO", "common"), // 흑착색 (사삼회철 피막)
    ENI("무전해 니켈도금","ENI", "common"), // 무전해 니켈도금
    ZIN("전기아연도금","ZIN", "common"), // 전기아연도금
    CHR("크롬 도금","CHR", "common"), // 크롬 도금
    NIC("니켈 도금","NIC", "common"), // 니켈 도금
    HCR("경질 크롬 도금","HCR", "common"), // 경질 크롬 도금
    EPP("전해연마","EPP", "common"), // 전해연마
    WAS("백색 아노다이징(반광)","WAS", "common"), // 백색 아노다이징 (뱐광)
    BAS("흑색 아노다이징(반광)","BAS", "common"), // 흑색 아노다이징 (반광)
    WAM("백색 아노다이징(무광)","WAM", "common"), // 백색 아노다이징 (무광)
    BAM("흑색 아노다이징(무광)","BAM", "common"), // 흑색 아노다이징 (무광)
    HAM("경질 아노다이징(국방)", "HAM", "common") // 경질 아노다이징 (국방)
    ;

    private final String label;
    private final String value;
    private final String type; // metal, cnc, common

    public static List<OptionDTO.OptionType> getSurfaceList() {
        return Arrays.stream(Surface.values())
                .map(surface -> (OptionDTO.OptionType) new OptionDTO.Options(surface.getLabel(), surface.getValue(), surface.getType()))
                .toList();
    }

}
