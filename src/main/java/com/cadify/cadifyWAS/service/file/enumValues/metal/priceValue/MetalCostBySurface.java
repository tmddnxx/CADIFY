package com.cadify.cadifyWAS.service.file.enumValues.metal.priceValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MetalCostBySurface {

    NA("NA", 0), // 없음
    PWC("PWC", 2800), // 분체도장
    BLO("BLO", 0), // 흑착색 (사삼회철 피막)
    ENI("ENI", 3000), // 무전해 니켈도금
    ZIN("ZIN", 3000), // 전기아연도금
    CHR("CHR", 0), // 크롬 도금
    NIC("NIC", 0), // 니켈 도금
    HCR("HCR", 0), // 경질 크롬 도금
    EPP("EPP", 3000), // 전해연마
    WAS("WAS", 0), // 백색 아노다이징 (뱐광)
    BAS("BAS", 0), // 흑색 아노다이징 (반광)
    WAM("WAM", 0), // 백색 아노다이징 (무광)
    BAM("BAM", 0), // 흑색 아노다이징 (무광)
    HAM("HAM", 2800), // 경질 아노다이징 (국방)
    ;

    private final String surface; // 표면처리
    private final int cost; // kg당 단가


    public static Double getCostBySurface(String surface, double kg){
        Double cost = 0.0;
        if(kg < 1.0) kg = 1;

        for(MetalCostBySurface m : MetalCostBySurface.values()){
            if(m.surface.equals(surface)){
                cost = m.cost * kg;
            }
        }

        return cost;
    }

}
