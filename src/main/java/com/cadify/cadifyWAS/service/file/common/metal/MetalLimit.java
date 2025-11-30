package com.cadify.cadifyWAS.service.file.common.metal;

import com.cadify.cadifyWAS.model.dto.files.CostDTO;
import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.cadify.cadifyWAS.service.file.common.CommentType;
import com.cadify.cadifyWAS.service.file.common.FileCommon;
import com.cadify.cadifyWAS.service.file.common.MethodType;
import com.cadify.cadifyWAS.service.file.enumValues.metal.defaultValue.MetalCSSize;
import com.cadify.cadifyWAS.service.file.enumValues.metal.defaultValue.MetalHoleSize;
import com.cadify.cadifyWAS.service.file.enumValues.metal.defaultValue.MetalMaterialDensity;
import com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.bending.*;
import com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.hole.*;
import com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.material.MetalBendSizeByMaterial;
import com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.material.MetalProcessSizeByMaterial;
import com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.material.MetalRBendSizeByMaterial;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class MetalLimit {

    /*-------------------------------- 업로드 시 검사 ---------------------------*/
    // 타입 찾기
    public static String extractMetalType(JsonNode partsArray) {
        JsonNode body = partsArray.get(0).get("bodies").get(0);
        return body.get("type").asText();
    }

    // metaJson에서 위험코드 찾기
    public static List<Integer> extractWarningDetails(JsonNode partsArray) {
        JsonNode body = partsArray.get(0).get("bodies").get(0);
        // code 값 가져오기
        JsonNode codes = body.get("codes");
        if (codes != null && codes.isArray() && !codes.isEmpty()) {
            List<Integer> codeList = new ArrayList<>();
            for (JsonNode code : codes) {
                if (code.isInt()) {
                    codeList.add(code.asInt());
                }
            }
            return codeList;
        }
        // code가 없거나 parts가 없으면 null 반환
        return null;
    }

    // metaJson에서 두께 찾기
    public static double extractThickness(JsonNode partsArray) {
        double thickness = 0.0;
        JsonNode body = partsArray.get(0).get("bodies").get(0);
        JsonNode thicknessNode = body.get("thickness");
        if (thicknessNode != null) {
            thickness = Math.round(thicknessNode.asDouble() * 10) / 10.0;
            return thickness;
        }

        return 0;
    }

    // metaJson에서 홀 찾기
    public static String extractHole(JsonNode partsArray, List<Estimate.ErrorDetail> errorDetailList) {
        List<OptionDTO.Hole> holeList = new ArrayList<>();
        JsonNode body = partsArray.get(0).get("bodies").get(0);

        JsonNode drilledHoles = body.path("drilledHoles");
        if(!drilledHoles.isNull() && !drilledHoles.isMissingNode()){
            for(JsonNode data : drilledHoles){
                if(!data.isNull()){
                    if(data.has("counterboreDiameter")){
                        errorDetailList.add(
                                Estimate.ErrorDetail.builder()
                                        .type("COUNTERBORE")
                                        .message("Counter Bore 가공이 감지되었습니다. \nCounter Bore 형상을 제거하거나 Counter Sink로 수정해주세요")
                                        .data(null)
                                        .build()
                        );
                    }

                    double diameter = Math.floor(data.path("diameter").asDouble(0) * 100) / 100;
                    int count = data.path("count").asInt();
                    JsonNode faceIds = data.path("faceIds");
                    List<List<Integer>> faceIdsList = new ArrayList<>();
                    if (faceIds.isArray()) {
                        for (JsonNode faceId : faceIds) {
                            List<Integer> faceIdList = new ArrayList<>();
                            for (JsonNode id : faceId) {
                                faceIdList.add(id.asInt());
                            }
                            faceIdsList.add(faceIdList);
                        }
                    }
                    double csDiameter = Math.floor(data.path("countersinkDiameter").asDouble(0) * 100) / 100;
                    double csAngle = Math.round(data.path("countersinkAngle").asDouble(0) * 100.0) / 100.0;

                    String type = null;
                    if (csDiameter != 0) {
                        type = MetalCSSize.getTypeByCounterSink(diameter, csDiameter);
                    } else {
                        type = MetalHoleSize.getTypeByHoleSize(diameter);
                    }

                    OptionDTO.Hole holeDTO = null;
                    if (type != null && csDiameter == 0) { // 홀만 존재함
                        holeDTO = OptionDTO.Hole.builder()
                                .faceIds(faceIdsList)
                                .type(type)
                                .count(count)
                                .diameter(diameter)
                                .build();

                    } else if (type != null && csDiameter != 0) { // 정상 카운터싱크가 존재함
                        holeDTO = OptionDTO.Hole.builder()
                                .faceIds(faceIdsList)
                                .type(type)
                                .count(count)
                                .diameter(diameter)
                                .csDiameter(csDiameter)
                                .csAngle(csAngle)
                                .build();

                    } else { // cs의 직경이 올바르지 않을때
                        errorDetailList.add(
                                Estimate.ErrorDetail.builder()
                                        .type("CSDiameterError")
                                        .message("카운터 싱크가 규격에 맞지 않습니다. \n기술 매뉴얼에 있는 카운터 싱크 규격 표를 참고하여 모델링 해주세요")
                                        .build()
                        );
                    }

                    // 카운터 싱크가 존재함 //
                    if (csDiameter != 0) {
                        Map<String, Object> errorMap = new HashMap<>();
                        errorMap.put("faceIds", faceIds);
                        errorMap.put("csAngle", csAngle);

                        // 카운터싱크 각도 에러
                        if(csAngle != 90){
                            errorDetailList.add(
                                    Estimate.ErrorDetail.builder()
                                            .type("CSAngleError")
                                            .message("ISO,JIS 규격에 따라 카운터싱크 각도는 90도로 모델링해주세요.")
                                            .data(errorMap)
                                            .build()
                            );
                        }
                    }
                    if (holeDTO != null) {
                        holeList.add(holeDTO);
                    }
                }
            }
        }

        if(holeList.isEmpty()) return null; // 홀이 없으면 null

        return FileCommon.convertListToString(holeList);
    }

    // metaJson에서 최소 한계치 오류 찾기
    public static String extractLimitError(JsonNode partsArray, double thickness, List<Estimate.ErrorDetail> errorDetailList) throws Exception {
        if(partsArray != null){
            if(partsArray.size() > 1) throw new Exception("두 개 이상의 파일이 결합된 어셈블리 파일입니다. \n1개의 단품만 업로드해주세요.");

            for(JsonNode part : partsArray){
                JsonNode bodyArray = part.get("bodies");
                if(bodyArray != null){
                    for(JsonNode body : bodyArray){
                        JsonNode cutoutArray = body.path("cutouts");
                        JsonNode drillHoles = body.path("drilledHoles");

                        /*홀 최소 직경 한계치 구함 */
                        if(!cutoutArray.isNull() && !drillHoles.isNull() && !cutoutArray.isMissingNode() && !drillHoles.isMissingNode()){
                            handleMinDiameter(cutoutArray, thickness, errorDetailList, drillHoles);
                        }

                        JsonNode bendsArray = body.path("bends");
                        if(bendsArray != null && !bendsArray.isMissingNode()){
                            for(JsonNode bend : bendsArray){
                                if(!bend.isNull()){
                                    // 절곡 폭 한계치 찾기
                                    handleBendingWidth(bend, thickness, errorDetailList);

                                    // 절곡 예각 최소 각도
                                    handleBendingAngle(bend, thickness, errorDetailList);

                                    JsonNode clearances = bend.path("clearances");
                                    if(!clearances.isNull() && !clearances.isMissingNode()){
                                        for(JsonNode data : clearances){
                                            if(!data.isNull()){
                                                // 절곡선에 평행한 접촉부 유무 확인
                                                handleBendParallel(data, errorDetailList);
                                                // 절곡부 최소높이 한계치 찾기
                                                handleMinHeight(bend, data, thickness, errorDetailList);
                                            }
                                        }
                                    }

                                    // 홀과 절곡 최소 거리
                                    handleMinDistance(bend, thickness, errorDetailList);
                                }
                            }
                        }
                    }
                }
            }
        }
        return FileCommon.convertListToString(errorDetailList);
    }

    // 절곡선에 평행한 접촉부의 유무확인
    private static void handleBendParallel(JsonNode data, List<Estimate.ErrorDetail> errorDetailList){

        boolean hasSupportEdge = data.path("hasSupportEdge").asBoolean();
        if(!hasSupportEdge){
            Map<String, Object> dataObj = new HashMap<>();
            dataObj.put("BendId", data.get("bendId"));
            dataObj.put("HasSupportEdge", false);

            String msg = "절곡 작업을 위해서는 절곡선과 평행한 엣지가 필요합니다.";

            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("BEND_PARALLEL")
                            .message(msg)
                            .data(dataObj)
                            .build()
            );
        }
    }

    // 예각 절곡의 최소각도
    private static void handleBendingAngle(JsonNode bend, double thickness, List<Estimate.ErrorDetail> errorDetailList){
        double angle = 180 - ((bend.path("angle").asDouble() * 10) /10);
        Double minAngle = MetalBendingAngle.getAngleByThickness(thickness);
        if(minAngle == null){
            return;
        }
        if(angle >= minAngle){ // 내각이 최소치보다 크면 정상
            return;
        }

        Map<String, Object> dataObj = new HashMap<>();
        dataObj.put("FaceIds", bend.get("ids"));
        dataObj.put("Angle", angle);
        dataObj.put("MinAngle", minAngle);

        String msg = "예각 절곡 각도가 " + angle + "도로 너무 작습니다. " +
                "\n최소 " + minAngle + "도 이상으로 모델링 해주세요.";

        errorDetailList.add(
                Estimate.ErrorDetail.builder()
                        .type("BENDING_ANGLE")
                        .message(msg)
                        .data(dataObj)
                        .build()
        );

    }


    // 절곡 최소 폭
    private static void handleBendingWidth(JsonNode bend, double thickness, List<Estimate.ErrorDetail> errorDetailList){
        double grossLength = (double) Math.round(bend.path("grossLength").asDouble() * 10) / 10;
        Double minGrossLength = MetalBendingWidth.getMinLengthByThickness(thickness);
        Double maxGrossLength = MetalBendingWidth.getMaxLengthByThickness(thickness);

        // 등록된 한계치 값이 없는경우 return
        if(minGrossLength == null || maxGrossLength == null){
            return;
        }
        // 정상적인 폭인 경우 return
        if(grossLength >= minGrossLength && grossLength <= maxGrossLength){
            return;
        }

        Map<String, Object> dataObj = new HashMap<>();
        dataObj.put("FaceIds", bend.get("ids"));
        dataObj.put("GrossLength", grossLength);
        dataObj.put("MinGrossLength", minGrossLength);
        dataObj.put("MaxGrossLength", maxGrossLength);

        String msg = "현재 절곡 폭 " + grossLength + "mm는 한계치를 벗어났습니다. \n" +
                "절곡 폭을 5mm ~ 1200mm 사이로 수정해주세요";

        errorDetailList.add(
                Estimate.ErrorDetail.builder()
                        .type("BENDING_WIDTH")
                        .message(msg)
                        .data(dataObj)
                        .build()
        );

    }

    // 홀 최소직경 (round, slot, rect)
    private static void handleMinDiameter(JsonNode cutoutArray, double thickness, List<Estimate.ErrorDetail> errorDetailList, JsonNode drillHoles){
        for(JsonNode cutout : cutoutArray){
            String type = cutout.path("type").asText();

            if(type.equals("RoundHole")){ // 라운드 홀인경우

                double diameter = (double) Math.round(cutout.path("diameter").asDouble(0) * 10) / 10 ;

                String msg = "현재 라운드 홀의 직경이 " + diameter + "mm로 너무 낮습니다. \n최소 직경은 두께의 0.8배인 "
                        + Math.round(thickness * 0.8 * 10) / 10 + "mm 이상으로 모델링 해주세요.";

                if (thickness * 0.3 <= diameter && diameter < thickness * 0.8) {
                    errorDetailList.add(
                            Estimate.ErrorDetail.builder()
                                    .type("ROUND_HOLE_REQUIRED")
                                    .message(msg)
                                    .data(EstimateDTO.ErrorFlag
                                            .builder()
                                            .flag(false)
                                            .comment(CommentType.COMMENT_TYPE1)
                                            .build()
                                    )
                                    .build()
                    );
                }

                if (diameter < thickness * 0.3) {
                    errorDetailList.add(
                            Estimate.ErrorDetail.builder()
                                    .type("ROUND_HOLE")
                                    .message(msg)
                                    .build()
                    );
                }

            } else if(type.equals("SlotHole")){ // 타원 홀인경우
                double sideA = (double) Math.round(cutout.path("sideA").asDouble(0) * 10) / 10;
                double sideB = (double) Math.round(cutout.path("sideB").asDouble(0) * 10) / 10;
                double min = Math.min(sideA, sideB);

                String msg = "현재 타원 홀의 최소 폭은 " + min + "mm로 너무 낮습니다. \n최소 폭은 두께인 "
                        + Math.round(thickness * 10) / 10 + "mm 이상으로 모델링 해주세요.";

                if (thickness * 0.3 <= min && min < thickness) {
                    errorDetailList.add(
                            Estimate.ErrorDetail.builder()
                                    .type("SLOT_HOLE_REQUIRED")
                                    .message(msg)
                                    .data(EstimateDTO.ErrorFlag
                                            .builder()
                                            .flag(false)
                                            .comment(CommentType.COMMENT_TYPE1)
                                            .build()
                                    )
                                    .build()
                    );
                }

                if (min < thickness * 0.3) {
                    errorDetailList.add(
                            Estimate.ErrorDetail.builder()
                                    .type("SLOT_HOLE")
                                    .message(msg)
                                    .build()
                    );
                }

            } else if(type.equals("RectHole")) { // 사각 홀인경우

                double sideA = (double) Math.round(cutout.path("sideA").asDouble(0) * 10) / 10;
                double sideB = (double) Math.round(cutout.path("sideB").asDouble(0) * 10) / 10;
                double min = Math.min(sideA, sideB);

                String msg = "현재 사각 홀의 최소 폭은 " + min + "mm로 너무 낮습니다. \n최소 폭은 두께인 "
                        + Math.round(thickness * 10) / 10 + "mm 이상으로 모델링 해주세요.";

                if (thickness * 0.3 <= min && min < thickness) {
                    errorDetailList.add(
                            Estimate.ErrorDetail.builder()
                                    .type("RECT_HOLE_REQUIRED")
                                    .message(msg)
                                    .data(EstimateDTO.ErrorFlag
                                            .builder()
                                            .flag(false)
                                            .comment(CommentType.COMMENT_TYPE1)
                                            .build()
                                    )
                                    .build()
                    );
                }

                if (min < thickness * 0.3) {
                    errorDetailList.add(
                            Estimate.ErrorDetail.builder()
                                    .type("RECT_HOLE")
                                    .message(msg)
                                    .build()
                    );
                }
            }

//                    /* 사각홀 최소 코너 R */
//                    if(segments.has("radius")){
//                        double radius = (double) Math.round(segments.path("radius").asDouble(0) * 100) / 100;
//                        if(radius <= 0.5){ // 최소 값 0.5 이상이면 정상
//                            Map<String, Object> dataObj = new HashMap<>();
//                            dataObj.put("LineIds", segments.get("id"));
//                            dataObj.put("Radius", radius);
//
//                            errorDetailList.add(
//                                    Estimate.ErrorDetail.builder()
//                                            .type("RectHoleCornerR")
//                                            .message("사각홀의 최소 코너 R은 0.5mm 이상이여야 합니다.")
//                                            .data(dataObj)
//                                            .build()
//                            );
//                        }
//                    }
//                }

//            }else{ // Arbitrary ( 기타 )
//
//                for(JsonNode segments : segmentsArray) {
//                    /* 다각홀 최소 코너 R */
//                    if(segments.has("radius") && segments.has("nextSegment")) {
//                        double radius = (double) Math.round(segments.path("radius").asDouble(0) * 100) / 100;
//
//                        if (radius <= 0.5) { // 최소 값 0.5 이상이면 정상
//                            Map<String, Object> dataObj = new HashMap<>();
//                            dataObj.put("LineIds", segments.get("id"));
//                            dataObj.put("Radius", radius);
//
//                            errorDetailList.add(
//                                    Estimate.ErrorDetail.builder()
//                                            .type("RectHoleCornerR")
//                                            .message("사각홀의 최소 코너 R은 0.5mm 이상이여야 합니다.")
//                                            .data(dataObj)
//                                            .build()
//                            );
//                        }
//                    }
//
//                }
//            }
        }
    }

    // 절곡부 최소높이
    private static void handleMinHeight(JsonNode bend, JsonNode data, double thickness, List<Estimate.ErrorDetail> errorDetailList) {
        double innerRadius = (double) Math.round(bend.path("innerRadius").asDouble() * 100) / 100;
        double height = (Math.round(data.path("maximumFlangeLength").asDouble() * 10) / 10.0) + thickness + innerRadius;

        if (thickness * 3 <= height && height < thickness * 4.5) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("MIN_HEIGHT_REQUIRED")
                            .message("현재 최소 절곡 높이가 " + height + "mm로 너무 낮습니다. \n최소 절곡 높이는 두께의 4.5배인 "
                                    + (thickness * 4.5) + "mm 이상으로 모델링 해주세요.")
                            .data(EstimateDTO.ErrorFlag
                                    .builder()
                                    .flag(false)
                                    .comment(CommentType.COMMENT_TYPE1)
                                    .build()
                            )
                            .build()
            );
            return;
        }

        if (height < thickness * 3) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("MIN_HEIGHT")
                            .message("현재 최소 절곡 높이가 " + height + "mm로 너무 낮습니다. \n최소 절곡 높이는 두께의 4.5배인 "
                                    + (thickness * 4.5) + "mm 이상으로 모델링 해주세요.")
                            .build()
            );
        }
    }

    // 일반절곡일때 홀과 절곡부 최소거리
    private static void handleMinDistance(JsonNode bend, double thickness, List<Estimate.ErrorDetail> errorDetailList){
        double innerRadius = (double) Math.round(bend.path("innerRadius").asDouble(0.0) * 100) / 100;
        if(innerRadius == 0.0) return;

        JsonNode clearances = bend.path("clearances");

        if(clearances != null && !clearances.isNull() && !clearances.isMissingNode()){
            for(JsonNode data : clearances) {

                if (!data.isNull()) {

                    double closestCutOut = Math.round(data.path("closestCutOut").asDouble(0.0) * 10) / 10.0;

                    if(closestCutOut == 0.0) continue;

                    if (thickness <= closestCutOut && closestCutOut < thickness * 1.5) {
                        errorDetailList.add(
                                Estimate.ErrorDetail.builder()
                                        .type("MIN_DISTANCE_REQUIRED")
                                        .message("현재 최소 절곡 거리가 " + closestCutOut + "mm로 너무 낮습니다. \n" +
                                                "최소 절곡 거리는 두께의 1.5배인 " + (thickness * 1.5) + "mm 이상으로 모델링 해주세요.")
                                        .data(EstimateDTO.ErrorFlag
                                                .builder()
                                                .flag(false)
                                                .comment(CommentType.COMMENT_TYPE2)
                                                .build()
                                        )
                                        .build());

                        return;
                    }

                    if (closestCutOut < thickness) {
                        errorDetailList.add(
                                Estimate.ErrorDetail.builder()
                                        .type("MIN_DISTANCE")
                                        .message("현재 최소 절곡 거리가 " + closestCutOut + "mm로 너무 낮습니다. \n" +
                                                "최소 절곡 거리는 두께의 1.5배인 " + (thickness * 1.5) + "mm 이상으로 모델링 해주세요.")
                                        .build()
                        );
                    }
                }
            }
        }
    }

    /* ------------------------------옵션 수정 시 검사 --------------------------------*/
    // 재질과 두께별 탭 사이즈 확인
    public static boolean checkTapSizeByMaterialAndThickness(String holeJson, double thickness, String material, List<Estimate.ErrorDetail> errorDetailList) throws JsonProcessingException {
        if (holeJson == null || holeJson.isEmpty()) return false;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(holeJson);

        if(material.contains("SUS")){ // 스테인리스
            if(!material.equals("SUS304_No1") && !material.equals("SUS304_2B")){
                material = "SUS304_POLISH";
            }
        }else if(material.contains("AL")){ // 알류미늄
            material = "AL5052";
        }else{ // 스틸계열
            if(!material.equals("SECC")){
                material = "STEEL";
            }
        }

        for(JsonNode json : root){
            String type = json.path("type").asText();

            if(!type.contains("CS") && !type.contains("hole")){ // 탭인경우 (카운터 싱크가 아닌경우)
                boolean flag = MetalTapSizeByMT.isMTypeSupported(material, thickness, type);

                if(!flag){ // 탭 사이즈가 안맞으면
                    String faceIds = json.get("faceIds").asText();
                    int count = json.get("count").asInt();
                    Map<String, Object> map = new HashMap<>();
                    map.put("faceIds", faceIds);
                    map.put("count", count);

                    errorDetailList.add(
                            Estimate.ErrorDetail.builder()
                                    .type("TapSizeError")
                                    .message("허용하지 않는 탭 사이즈입니다.")
                                    .data(map)
                                    .build()
                    );
                    throw new RuntimeException("허용하지 않는 탭 사이즈입니다.");
                }
            }
            else if(type.contains("CS")){ // 카운터 싱크일때
                boolean flag = MetalCSSizeByMT.isMTypeSupported(material, thickness, type);

                if(!flag){ // 카운터 싱크 사이즈가 안맞으면
                    String faceIds = json.path("faceIds").asText();
                    int count = json.get("count").asInt();
                    Map<String, Object> map = new HashMap<>();
                    map.put("faceIds", faceIds);
                    map.put("count", count);

                    errorDetailList.add(
                            Estimate.ErrorDetail.builder()
                                    .type("CSSizeError")
                                    .message("허용하지 않는 접시홀 사이즈입니다.")
                                    .data(map)
                                    .build()
                    );
                    throw new RuntimeException("허용하지 않는 접시홀 사이즈입니다.");
                }
            }
        }

        return true;
    }

    // 한계치 찾기
    public static String findLimitError(JsonNode partsArray, double thickness, String originMaterial, String majorMaterial, List<Estimate.ErrorDetail> errorDetailList, CostDTO.MetalCostDTO costDTO) {
        JsonNode body = partsArray.get(0).get("bodies").get(0);

        // kg 무게찾기
        double volume = body.path("flatOuterContourArea").asDouble() * thickness;
        costDTO.setKg(MetalMaterialDensity.calculateWeight(majorMaterial, volume));

        // 전체 가공길이 찾기
        costDTO.setTotalLength(body.path("flatTotalContourLength").asDouble(0.0));

        // 가공 가능 사이즈 찾기 obbLength, obbWidth
        handleSizeByMaterial(body, originMaterial, errorDetailList);

        int rBendCnt = 0; // r절곡 갯수
        int bendCnt = 0; // 일반 절곡 갯수

        JsonNode bendArray = body.path("bends");
        if(bendArray != null && !bendArray.isNull() && !bendArray.isMissingNode()){
            for(JsonNode bend : bendArray){
                if(bend != null && !bend.isNull()){

                    // R절곡일때
                    if(handleRBending(bend, thickness, majorMaterial, errorDetailList)){
                        rBendCnt++;
                        // R절곡비 찾기
                        MetalPrice.findRBendCost(bend, costDTO);

                        // R 절곡 가능 사이즈 체크
                        handleBendSizeByMaterial(body, originMaterial, thickness, errorDetailList, true);
                        // R절곡 최소 절곡 높이
                        handleRMinHeight(bend, thickness, majorMaterial, errorDetailList);
                        // 홀과 R절곡 최소 거리
                        handleRBendMinDistance(bend, thickness, majorMaterial, errorDetailList);
                    }else{ // 일반절곡일때
                        bendCnt++;
                        // 절곡비 찾기
                        MetalPrice.findBendCost(bend, thickness, costDTO);

                        // 절곡 가능 사이즈 체크
                        handleBendSizeByMaterial(body, originMaterial, thickness, errorDetailList, false);


                    }
                }
            }
            costDTO.setRBendCnt(rBendCnt);
            costDTO.setBendCnt(bendCnt);
        }

        return FileCommon.convertListToString(errorDetailList);
    }

    // 도장,도금용 리프팅 홀 찾기
    public static String findLiftHole(JsonNode partsArray, String material){
        JsonNode body = partsArray.get(0).get("bodies").get(0);

        double dx = body.get("bboxDx").asDouble();
        double dy = body.get("bboxDy").asDouble();
        double dz = body.get("bboxDz").asDouble();
        double maxD = Math.max(Math.max(dx, dy), dz);

        double volume = body.get("volume").asDouble();
        Double kg = MetalMaterialDensity.calculateWeight(material, volume);
        if(kg == null){
            return "올바른 재질을 선택해주세요";
        }

        JsonNode cutouts = body.get("cutouts");
        if(cutouts != null && !cutouts.isNull()){
            int holeCnt = 0; // 홀 갯수

            for(JsonNode cutout : cutouts){
                double area = cutout.path("area").asDouble();
                int count = cutout.path("count").asInt();
                String type = cutout.path("type").asText(); // 홀 타입
                boolean isCounterSink = false;
                JsonNode faceIds = cutout.path("faceIds");
                for (JsonNode faceId : faceIds) {
                    if (faceId.isArray() && faceId.size() == 2) {
                        isCounterSink = true;
                        break;
                    }
                }

                if (
                        (type.equals("RoundHole") && area > 12.56) || // 라운드 홀
                        (type.equals("RectHole") && area > 16) || // 사각홀
                        (type.equals("SlotHole") && area > 12.96) || // 타원 홀
                        (type.equals("Arbitrary") &&
                        ((isCounterSink && area > 12.56) || // 카운터싱크
                        (!isCounterSink && area > 30))) // 다각홀
                ) {
                    holeCnt += count;
                }

            }
            if(kg >= 8 && holeCnt < 2){
                return "모델의 중량이 8kg 이상이므로 관통홀을 2곳 이상 모델링해주세요.";
            }
            if(maxD >= 420 && holeCnt < 2){
                return "모델의 가장 긴쪽이 420mm 이상이므로 관통홀을 2곳 이상 모델링해주세요.";
            }
            if(holeCnt < 1){
                return "표면처리를 위해 리프팅 홀이 필요합니다. \n리프팅 홀 규격 페이지를 참고해서 모델링해주세요.";
            }
        }
        return null;
    }

    // 일반절곡/R절곡 타입 재질별 가공 가능 사이즈 찾기
    private static void handleBendSizeByMaterial(JsonNode body, String material, double thickness, List<Estimate.ErrorDetail> errorDetailList, boolean type) throws RuntimeException {

        int obbLength = body.path("flatObbLength").asInt();
        int obbWidth = body.path("flatObbWidth").asInt();

        if(!type){ // type이 true면 R절곡, false면 일반절곡

            /* 일반절곡이 가능한 두께 검사*/
            Double maxThickness = MetalBendByThickness.getThickness(material); // 절곡 가능 두께
            if(maxThickness == null) throw new RuntimeException("잘못된 재질입니다.");
            if(thickness > maxThickness){
                errorDetailList.add(
                        Estimate.ErrorDetail.builder()
                                .type("BendThickError")
                                .message("절곡 가능한 두께 "+ maxThickness + "mm를 벗어났습니다. 소재에 따른 절곡 가능 두께를 확인해 주세요.")
                                .data(null)
                                .build()
                );
            }

            /* 일반절곡이 가능한 형상 크기 검사 */
            Integer[] sizes = MetalBendSizeByMaterial.getSize(material);
            if(sizes == null) throw new RuntimeException("잘못된 재질입니다.");

            int minLength = sizes[0];
            int minWidth = sizes[1];
            int maxLength = sizes[2];
            int maxWidth = sizes[3];

            boolean case1 = (minLength <= obbLength && obbLength <= maxLength) &&
                    (minWidth <= obbWidth && obbWidth <= maxWidth);

            boolean case2 = (minLength <= obbWidth && obbWidth <= maxLength) &&
                    (minWidth <= obbLength && obbLength <= maxWidth);

            if(!case1 && !case2) { // 범위를 모두 벗어난 경우 예외처리

                Map<String, Object> map = new HashMap<>();
                map.put("minLength", minLength);
                map.put("minWidth", minWidth);
                map.put("maxLength", maxLength);
                map.put("maxWidth", maxWidth);

                errorDetailList.add(
                        Estimate.ErrorDetail.builder()
                                .type("BendSizeError")
                                .message("절곡 가능한 크기를 벗어났습니다. 소재에 따른 절곡 가능 사이즈를 확인해 주세요.")
                                .data(map)
                                .build()
                );
            }

        }else{ // R절곡 타입 재질별 가공 가능 사이즈 찾기

            /* R 절곡이 가능한 두께 검사*/
            Double maxThickness = MetalRBendByThickness.getThickness(material); // 절곡 가능 두께
            if(maxThickness == null) throw new RuntimeException("잘못된 재질입니다.");
            if(thickness > maxThickness){
                errorDetailList.add(
                        Estimate.ErrorDetail.builder()
                                .type("BendThickError")
                                .message("R 절곡 가능한 두께 "+ maxThickness + "mm를 벗어났습니다. 소재에 따른 R 절곡 가능 두께를 확인해 주세요.")
                                .data(null)
                                .build()
                );
            }


            /* R 절곡이 가능한 형상 크기 검사*/
            Integer[] sizes = MetalRBendSizeByMaterial.getSize(material);
            if(sizes == null) throw new RuntimeException("잘못된 재질입니다.");

            int minLength = sizes[0];
            int minWidth = sizes[1];
            int maxLength = sizes[2];
            int maxWidth = sizes[3];

            boolean case1 = (minLength <= obbLength && obbLength <= maxLength) &&
                    (minWidth <= obbWidth && obbWidth <= maxWidth);

            boolean case2 = (minLength <= obbWidth && obbWidth <= maxLength) &&
                    (minWidth <= obbLength && obbLength <= maxWidth);

            if(!case1 && !case2) { // 범위를 모두 벗어난 경우 예외처리
                Map<String, Object> map = new HashMap<>();
                map.put("minLength", minLength);
                map.put("minWidth", minWidth);
                map.put("maxLength", maxLength);
                map.put("maxWidth", maxWidth);

                errorDetailList.add(
                        Estimate.ErrorDetail.builder()
                                .type("RBendSizeError")
                                .message("R 절곡 가능한 크기를 벗어났습니다. 소재에 따른 R 절곡 가능 사이즈를 확인해 주세요.")
                                .data(map)
                                .build()
                );
            }
        }
    }

    // Flat타입 재질별 가공 가능 사이즈 찾기
    private static void handleSizeByMaterial(JsonNode body, String material, List<Estimate.ErrorDetail> errorDetailList) throws RuntimeException {

        String type = body.path("type").asText();
        if(FileCommon.isSameMethod(type, MethodType.SHEET_METAL_FLAT)){ // 절곡없을때
            int obbLength = body.path("flatObbLength").asInt();
            int obbWidth = body.path("flatObbWidth").asInt();

            Integer[] sizes = MetalProcessSizeByMaterial.getSize(material); // 재질 별 사이즈 찾기
            if(sizes == null) throw new RuntimeException("잘못된 재질입니다.");

            int minLength = sizes[0];
            int minWidth = sizes[1];
            int maxLength = sizes[2];
            int maxWidth = sizes[3];

            boolean case1 = (minLength <= obbLength && obbLength <= maxLength) &&
                    (minWidth <= obbWidth && obbWidth <= maxWidth);

            boolean case2 = (minLength <= obbWidth && obbWidth <= maxLength) &&
                    (minWidth <= obbLength && obbLength <= maxWidth);

            if(!case1 && !case2){ // 범위를 모두 벗어난 경우 예외처리
                Map<String, Object> map = new HashMap<>();
                map.put("minLength", minLength);
                map.put("minWidth", minWidth);
                map.put("maxLength", maxLength);
                map.put("maxWidth", maxWidth);

                errorDetailList.add(
                        Estimate.ErrorDetail.builder()
                                .type("ProcessSizeError")
                                .message("가공 가능한 크기를 벗어났습니다. 소재에 따른 가공 가능 사이즈를 확인해 주세요.")
                                .data(map)
                                .build()
                );
            }
        }
    }

    // R절곡 판단 (true = R절곡)
    private static boolean handleRBending(JsonNode bend, double thickness, String material, List<Estimate.ErrorDetail> errorDetailList) {
        double innerRadius = bend.path("innerRadius").asDouble(0.0);
        innerRadius = Math.round(innerRadius * 100) / 100.0; // 소수점 2자리까지

        Double minRadius = MetalRBendingJudge.getMinRadiusByMaterial(material);
        Double maxRadius = MetalRBendingJudge.getMaxRadiusByMaterial(material);

        if(minRadius == null || maxRadius == null) throw new RuntimeException("잘못된 재질입니다,");

        if((innerRadius > thickness && innerRadius < minRadius) || innerRadius > maxRadius){ // 에러
            Map<String, Object> dataObj = new HashMap<>();
            dataObj.put("FaceIds", bend.get("ids"));
            dataObj.put("InnerRadius", innerRadius);
            dataObj.put("MinRadius", minRadius);
            dataObj.put("MaxRadius", maxRadius);

            String msg = "절곡의 R값이 정상적이지 않습니다. " +
                    "\nR절곡 희망 시 절곡 R 값은 기술 메뉴얼 페이지를 참고해서 모델링 해주세요";

            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("RBendingError")
                            .message(msg)
                            .data(dataObj)
                            .build()
            );
            log.info("절곡 에러입니다.");
        }else if(innerRadius >= minRadius && innerRadius <= maxRadius){ // R절곡으로 판단
            log.info("R절곡입니다.");
            return true;
        }else if(innerRadius <= thickness){ // 일반절곡으로 판단
            log.info("일반절곡입니다.");
            return false;
        }

        return false;
    }

    // R절곡일때 홀과 절곡부 최소거리
    private static void handleRBendMinDistance(JsonNode bend, double thickness, String material, List<Estimate.ErrorDetail> errorDetailList){
        JsonNode clearances = bend.path("clearances");
        if(clearances != null && !clearances.isNull() && !clearances.isMissingNode()){
            for (JsonNode data : clearances) {
                if(data != null && !data.isNull()){
                    double distance = Math.floor(data.path("closestCutout").asDouble(0) * 1000) / 1000.0;
                    Double guarantee = MetalRBendMinDistance.getGuaranteeByMaterial(material, thickness); // 보증치
                    Double minDistance = MetalRBendMinDistance.getMinDistanceByMaterial(material, thickness); // 한계치
                    if(guarantee == null || minDistance == null || distance == 0){
                        return;
                    }

                    // 최소거리 만족 못하면 에러
                    if(distance < minDistance){
                        Map<String, Object> dataObj = new HashMap<>();
                        dataObj.put("FaceIds", bend.get("ids"));
                        dataObj.put("Distance", distance);
                        dataObj.put("MinDistance", minDistance);

                        String msg = "홀과 절곡부의 최소 거리를 만족하지 않습니다. \n" +
                                "현재 거리 " + distance + "mm는 최소 거리 " + minDistance +"mm 이상이어야 합니다.";

                        errorDetailList.add(
                                Estimate.ErrorDetail.builder()
                                        .type("RBendMinDistance")
                                        .message(msg)
                                        .data(dataObj)
                                        .build()
                        );
                    }
                    // 보증치보다 작을경우 안내메시지
                    if(minDistance < distance && distance < guarantee){
                        Map<String, Object> dataObj = new HashMap<>();
                        dataObj.put("FaceIds", bend.get("ids"));
                        dataObj.put("Distance", distance);
                        dataObj.put("MinDistance", minDistance);
                        dataObj.put("Guarantee", guarantee);

                        String msg = "현재 가공거리 " + distance + "mm는 보증치인 " + guarantee + "mm를 만족하지 않습니다. \n" +
                                "정확한 가공을 위해서는 보증치보다 가공거리가 길게 모델링해주세요.";

                        errorDetailList.add(
                                Estimate.ErrorDetail.builder()
                                        .type("RBendMinDistance")
                                        .message(msg)
                                        .data(dataObj)
                                        .build()
                        );
                    }
                }
            }
        }
    }

    // R절곡일때 최소 절곡 높이 한계치
    private static void handleRMinHeight(JsonNode bend, double thickness, String material, List<Estimate.ErrorDetail> errorDetailList){
        JsonNode clearances = bend.path("clearances");
        if(clearances != null && !clearances.isNull() && !clearances.isMissingNode()){
            for(JsonNode data : clearances){
                if(data != null && !data.isNull()){
                    double height = (double) Math.round(data.path("maximumFlangeLength").asDouble(0.0) * 100) / 100;

                    Double minHeight = MetalRBendMinHeight.getHeightByMaterial(material, thickness);
                    if(minHeight == null){
                        return;
                    }
                    if(height < minHeight){
                        Map<String, Object> dataObj = new HashMap<>();
                        dataObj.put("FaceIds", bend.get("ids"));
                        dataObj.put("Height", height);
                        dataObj.put("MinHeight", minHeight);

                        String msg = "한계치보다 작은 수치는 가공불가합니다.";

                        errorDetailList.add(
                                Estimate.ErrorDetail.builder()
                                        .type("RMinHeight")
                                        .message(msg)
                                        .data(dataObj)
                                        .build()
                        );
                    }
                }
            }
        }
    }
    
    // bbox 수치 추출
    public static OptionDTO.BBox extractBBox(JsonNode parts) {
        JsonNode body = parts.get(0).path("bodies").get(0);

        double dx = (double) Math.round(body.path("bboxDx").asDouble() * 10) / 10;
        double dy = (double) Math.round(body.path("bboxDy").asDouble() * 10) / 10;
        double dz = (double) Math.round(body.path("bboxDz").asDouble() * 10) / 10;

        return OptionDTO.BBox.builder()
                .x(dx)
                .y(dy)
                .z(dz)
                .build();
    }
}
