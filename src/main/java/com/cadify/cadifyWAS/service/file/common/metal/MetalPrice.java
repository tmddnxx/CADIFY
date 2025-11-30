package com.cadify.cadifyWAS.service.file.common.metal;

import com.cadify.cadifyWAS.model.dto.files.CostDTO;
import com.cadify.cadifyWAS.service.file.enumValues.metal.priceValue.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MetalPrice {

    public static double getPriceCalc(CostDTO.MetalCostDTO costDTO){

        // 소재비
        Double materialCost = MetalCostByKg.getCostByKg(costDTO.getMaterial(), costDTO.getKg());

        // 가공비
        Double processCost = MetalCostByProcess.getCostByProcess(costDTO.getMaterial(), costDTO.getThickness(), costDTO.getTotalLength());

        // 탭 + 카운터싱크 비용 (사이즈 * 단가 * 개수)
        double totalHoleCost = getTotalHoleCost(costDTO.getHoleJson());
        
        // 절곡비
        int totalBendCost = costDTO.getBendCost() + costDTO.getRBendCost();

        // 표면처리
        Double surfaceCost = MetalCostBySurface.getCostBySurface(costDTO.getSurface(), costDTO.getKg());

        // 총 가격 (납기일 제외, 단납기면 총 가격 * 1.2)
        double totalCost = materialCost + processCost + totalHoleCost + totalBendCost + surfaceCost;

        return totalCost;
    }
    
    // 탭, CS 사이즈 별 가격 측정 (사이즈 * 수량 * 단가)
    private static double getTotalHoleCost(String holeJson) {
        if (holeJson == null || holeJson.isEmpty()) return 0.0;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode holeNodes = null;
        try{
            holeNodes = objectMapper.readTree(holeJson);
        }catch (JsonProcessingException e){
            throw new RuntimeException("홀 정보가 올바르지 않습니다.");
        }

        int tapCost = MetalCostByHole.getCostByHole("TAP"); // 개당 단가
        int csCost = MetalCostByHole.getCostByHole("CS"); // 개당 단가
        double totalCost = 0.0;

        for (JsonNode holeObj : holeNodes) {
            String type = holeObj.path("type").asText(); // 구멍 타입
            int cnt = holeObj.path("count").asInt(); // 구멍 개수
            if (type.equals("hole")) continue;

            if (type.startsWith("CS_")) {
                double size = Double.parseDouble(type.split("CS_M")[1]); // 구멍 사이즈
                totalCost += size * csCost * cnt;
            } else if (type.startsWith("M")) {
                double size = Double.parseDouble(type.replace("M", ""));
                totalCost += size * tapCost * cnt;
            }
        }

        return totalCost;
    }

    // 절곡비 찾기
    public static void findBendCost(JsonNode bend, double thickness, CostDTO.MetalCostDTO costDTO){
        double length = bend.path("grossLength").asDouble();
        int cost = MetalCostByBend.getCostByBend(thickness, length);
        int bendCost =  costDTO.getBendCost();
        bendCost += cost;
        costDTO.setBendCost(bendCost);
    }

    // R절곡비 찾기
    public static void findRBendCost(JsonNode bend, CostDTO.MetalCostDTO costDTO){
        double length = bend.path("grossLength").asDouble();
        int cost = MetalCostByRBend.getCostByBend(length);
        int bendCost = costDTO.getRBendCost();
        bendCost += cost;
        costDTO.setRBendCost(bendCost);
    }
}
