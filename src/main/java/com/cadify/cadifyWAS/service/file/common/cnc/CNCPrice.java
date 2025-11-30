package com.cadify.cadifyWAS.service.file.common.cnc;

import com.cadify.cadifyWAS.model.dto.files.CostDTO;
import com.cadify.cadifyWAS.service.file.enumValues.cnc.priceValue.CNCCostByKg;
import com.cadify.cadifyWAS.service.file.rabbitMQ.LogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CNCPrice {

    public static double getPriceByCnc(CostDTO.CnCCostDTO costDTO, CNCAxisAnalyzer.Result analysisResult, LogService logService){

        // 소재비
        Double materialCost = CNCCostByKg.getCostByKg(costDTO.getMaterial(), costDTO.getKg());
        log.info("소재비 : {}", materialCost);
        logService.add("소재비 : " + materialCost); // fixme : 테스트용

        // 캠 작업 시간
        CNCLimit.MachineTime machineTime = CNCLimit.calcMachineTime(costDTO.getBodyJson(), costDTO.getCncType(), analysisResult);
        double camTime = machineTime.getCamTime();
        double settingTime = machineTime.getSettingTime();
        log.info("캠 작업 시간 : {}", camTime);
        logService.add("캠 작업 시간 : " + camTime); // fixme : 테스트용
        log.info("세팅 시간 : {}", settingTime);
        logService.add("세팅 시간 : " + settingTime); // fixme : 테스트용

        // 챔퍼 작업시간
        double chamferTime = CNCLimit.calcChamferTime(costDTO.getBodyJson());
        log.info("챔퍼 작업 시간 : {}", chamferTime);
        logService.add("챔퍼 작업 시간 : " + chamferTime); // fixme : 테스트용

        // 가공 시간
        double processingTime = CNCLimit.calcProcessingTime(costDTO.getBodyJson(), costDTO, analysisResult);
        log.info("가공 시간 : {}", processingTime);
        logService.add("가공 시간 : " + processingTime); // fixme : 테스트용

        // 3d 가공시간
        double processing3DTime = CNCLimit.calc3DArea(analysisResult);
        log.info("3D 가공 시간 : {}", processing3DTime);
        logService.add("3D 가공 시간 : " + processing3DTime); // fixme : 테스트용

        CalcPrice calcPrice = CalcPrice.builder()
                .materialCost(materialCost)
                .camTime(camTime)
                .settingTime(settingTime)
                .chamferTime(chamferTime)
                .processingTime(camTime + settingTime + processingTime)
                .build();
        log.info("calcPrice : {}", calcPrice.toString());
        // 탭비용
        double tapCost = CNCLimit.calcTapCost(costDTO.getHoleJson());
        log.info("탭 비용 : {}", tapCost);
        logService.add("탭 비용 : " + tapCost); // fixme : 테스트용

        // 지그비용
        double jigCost = CNCLimit.calcJigCost(calcPrice, analysisResult);
        log.info("지그 비용 : {}", jigCost);
        logService.add("지그 비용 : " + jigCost); // fixme : 테스트용

        // 표면처리(후가공) 비용
        double surfaceCost = CNCLimit.calcSurfaceCost(costDTO);
        log.info("표면처리 비용 : {}", surfaceCost);
        logService.add("표면처리 비용 : " + surfaceCost); // fixme : 테스트용

        // 공차 비용
        double commonDiffCost = CNCLimit.calcCommonDiffCost(costDTO, calcPrice);
        log.info("공차 비용 : {}", commonDiffCost);
        logService.add("공차 비용 : " + commonDiffCost); // fixme : 테스트용

        // 표면거칠기 비용
        double roughnessCost = CNCLimit.calcRoughnessCost(costDTO, calcPrice);
        log.info("표면거칠기 비용 : {}", roughnessCost);
        logService.add("표면거칠기 비용 : " + roughnessCost); // fixme : 테스트용

        // 총 가공시간 비용
        double totalTimeCost = (camTime + settingTime + chamferTime + processingTime + processing3DTime) * 10;
        log.info("총 가공시간 비용 : {}", totalTimeCost);
        logService.add("총 가공시간 비용 : " + totalTimeCost); // fixme : 테스트용

        double totalPrice = materialCost + totalTimeCost + tapCost + jigCost + surfaceCost + commonDiffCost + roughnessCost;
        return totalPrice;
    }

    @Getter
    @Builder
    @ToString
    @AllArgsConstructor
    public static class CalcPrice {
        private double materialCost;
        private double camTime;
        private double settingTime;
        private double chamferTime;
        private double processingTime;
    }
}
