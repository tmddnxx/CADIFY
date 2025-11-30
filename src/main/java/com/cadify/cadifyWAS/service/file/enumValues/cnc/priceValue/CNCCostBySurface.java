package com.cadify.cadifyWAS.service.file.enumValues.cnc.priceValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CNCCostBySurface {

    NA("NA", 0), // 없음
    PWC("PWC", 6500), // 분체도장
    BLO("BLO", 6500), // 흑착색 (사삼회철 피막)
    ENI("ENI", 6500), // 무전해 니켈도금
    ZIN("ZIN", 0), // 전기아연도금
    CHR("CHR", 6500), // 크롬 도금
    NIC("NIC", 6500), // 니켈 도금
    HCR("HCR", 6500), // 경질 크롬 도금
    EPP("EPP", 0), // 전해연마
    WAS("WAS", 6500), // 백색 아노다이징 (뱐광)
    BAS("BAS", 6500), // 흑색 아노다이징 (반광)
    WAM("WAM", 6500), // 백색 아노다이징 (무광)
    BAM("BAM", 6500), // 흑색 아노다이징 (무광)
    HAM("HAM", 6500), // 경질 아노다이징 (국방)
    ;

    private final String surface; // 표면처리
    private final int cost; // kg당 단가


    public static Double getCostBySurface(String surface, double kg){
        Double cost = 0.0;
        if(kg < 1.0) kg = 1;

        for(CNCCostBySurface m : CNCCostBySurface.values()){
            if(m.surface.equals(surface)){
                cost = m.cost * kg;
            }
        }

        return cost;
    }
}
