package com.cadify.cadifyWAS.service.file.common.cnc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CNCAxisAnalyzer {

    // 표준 축을 상수로 정의
    private static final double[][] STANDARD_AXES = {
            {1, 0, 0}, {-1, 0, 0},
            {0, 1, 0}, {0, -1, 0},
            {0, 0, 1}, {0, 0, -1}
    };

    /**
     * JSON 데이터를 분석하고 결과 반환
     * @param jsonContent JSON 문자열
     * @return 분석 결과
     */
    public AnalysisResult analyzeJsonData(String jsonContent) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonData = mapper.readTree(jsonContent);

            // 데이터 구조 초기화
            Map<Integer, Set<String>> faceMachiningData = new HashMap<>();
            Map<Integer, String> faceTypeMap = new HashMap<>();
            Map<Integer, Double> faceDiameterMap = new HashMap<>();
            Map<Integer, Double> faceAreaMap = new HashMap<>();

            Set<Integer> processedFaceIds = new HashSet<>();
            int totalHoleCount = 0;
            int totalCylinderCount = 0; // 실린더 카운트 변수
            int totalShaftCount = 0;

            List<PlaneWithZeroDepth> planesWithZeroDepth = new ArrayList<>();
            Map<Integer, List<double[]>> planeEndMillingAxes = new HashMap<>();

            Set<Integer> threeDMachiningFaces = new HashSet<>();
            Set<Integer> specialJigFaces = new HashSet<>();
            Map<Integer, CylinderSideMillingData> cylinderSideMilling = new HashMap<>();

            // JSON 데이터 처리
            if (jsonData.has("parts")) {
                for (JsonNode part : jsonData.get("parts")) {
                    if (part.has("bodies")) {
                        for (JsonNode body : part.get("bodies")) {
                            if (body.has("features")) {
                                JsonNode features = body.get("features");

                                // 홀 처리
                                if (features.has("holes")) {
                                    for (JsonNode hole : features.get("holes")) {
                                        processHole(hole, faceMachiningData, faceTypeMap, faceDiameterMap,
                                                faceAreaMap, processedFaceIds);

                                        if (hole.has("faceIds") && hole.get("faceIds").size() > 0) {
                                            totalHoleCount++;
                                        }
                                    }
                                }

                                // 샤프트 처리
                                if (features.has("shafts")) {
                                    for (JsonNode shaft : features.get("shafts")) {
                                        processShaft(shaft, faceMachiningData, faceTypeMap, faceDiameterMap,
                                                faceAreaMap, processedFaceIds);

                                        if (shaft.has("faceIds") && shaft.get("faceIds").size() > 0) {
                                            totalShaftCount++;
                                        }
                                    }
                                }

                                // 가공면 처리
                                if (features.has("milledFaces3Axes")) {
                                    for (JsonNode face : features.get("milledFaces3Axes")) {
                                        // 실린더 카운트 증가를 위해 반환값 받아서 더함
                                        totalCylinderCount += processMilled3AxesFace(face, faceMachiningData, faceTypeMap,
                                                faceDiameterMap, faceAreaMap, threeDMachiningFaces,
                                                cylinderSideMilling, planesWithZeroDepth, planeEndMillingAxes);
                                    }
                                }

                                // 인식되지 않은 면 처리
                                if (body.has("unrecognized") && body.get("unrecognized").size() > 0) {
                                    for (JsonNode faceId : body.get("unrecognized")) {
                                        threeDMachiningFaces.add(faceId.asInt());
                                    }
                                }

                                // 축-면 매핑 생성
                                Map<String, AxisFaceGroup> axisFaceMap = createAxisFaceMap(faceMachiningData,
                                        faceTypeMap, threeDMachiningFaces);

                                // 우선순위로 축 정렬
                                List<String> sortedAxes = sortAxesByPriority(axisFaceMap);

                                // 최적 축 찾기
                                List<double[]> optimalAxes = findOptimalAxes(sortedAxes, axisFaceMap, faceMachiningData, faceTypeMap);

                                // 특수 지그와 3D 가공을 위한 실린더 처리
                                processCylindersForSpecialCases(cylinderSideMilling, threeDMachiningFaces,
                                        specialJigFaces, optimalAxes);

                                // 특수 지그가 필요한 면 확인
                                checkFacesForSpecialJigs(faceMachiningData, threeDMachiningFaces,
                                        specialJigFaces, optimalAxes);

                                // 축별 면 정리
                                Map<String, List<FaceData>> facesByAxis = organizeFacesByAxis(optimalAxes,
                                        faceMachiningData, faceTypeMap, faceDiameterMap,
                                        threeDMachiningFaces, specialJigFaces);

                                // 축별 최소 직경 계산
                                Map<String, Double> axisMinDiameters = calculateAxisMinDiameters(facesByAxis);

                                // 지그 요구사항 결정
                                Map<String, Boolean> jigRequirementsByAxis = determineJigRequirements(optimalAxes,
                                        planesWithZeroDepth);

                                // 필요한 총 지그 수 계산
                                int totalJigsNeeded = calculateTotalJigsNeeded(optimalAxes, jigRequirementsByAxis);
                                int standardJigsNeeded = calculateStandardJigsNeeded(optimalAxes, jigRequirementsByAxis);

                                // 특수 면의 면적 계산
                                double totalSpecialArea = calculateSpecialFaceArea(threeDMachiningFaces,
                                        specialJigFaces, faceAreaMap);

                                // 축 우선순위 정보 생성
                                List<AxisPriorityInfo> axisPriorityInfoList = createAxisPriorityInfoList(sortedAxes, axisFaceMap);

                                // 면 세부 정보 생성
                                List<FaceDetailsInfo> faceDetailsInfoList = createFaceDetailsInfoList(optimalAxes, facesByAxis);

                                // 최소 직경 정보 생성
                                List<AxisMinDiameterInfo> axisMinDiameterInfoList = createAxisMinDiameterInfoList(
                                        optimalAxes, axisMinDiameters);

                                // 지그 요구사항 정보 생성
                                List<JigRequirementInfo> jigRequirementInfoList = createJigRequirementInfoList(
                                        optimalAxes, jigRequirementsByAxis);

                                // 최적 축 정보 생성
                                List<OptimalAxisInfo> optimalAxisInfoList = createOptimalAxisInfoList(optimalAxes);

                                // 가공 정보 생성
                                MachiningInfo machiningInfo = MachiningInfo.builder()
                                        .totalHoleCount(totalHoleCount)
                                        .totalCylinderCount(totalCylinderCount)
                                        .totalShaftCount(totalShaftCount)
                                        .build();

                                // 모든 특수 면 결합
                                Set<Integer> allSpecialFaces = new HashSet<>();
                                allSpecialFaces.addAll(threeDMachiningFaces);
                                allSpecialFaces.addAll(specialJigFaces);
                                List<Integer> sortedSpecialFaces = new ArrayList<>(allSpecialFaces);
                                Collections.sort(sortedSpecialFaces);

                                // 3D 가공 정보 생성
                                ThreeDMachiningInfo threeDMachiningInfo = ThreeDMachiningInfo.builder()
                                        .specialFaces(sortedSpecialFaces)
                                        .totalSpecialArea(totalSpecialArea)
                                        .build();

                                // 최종 결과 생성 및 반환
                                return AnalysisResult.builder()
                                        .optimalAxes(optimalAxisInfoList)
                                        .machiningInfo(machiningInfo)
                                        .axisMinDiameters(axisMinDiameterInfoList)
                                        .jigRequirements(jigRequirementInfoList)
                                        .standardJigsNeeded(standardJigsNeeded)
                                        .axisPriorities(axisPriorityInfoList)
                                        .faceDetails(faceDetailsInfoList)
                                        .threeDMachining(threeDMachiningInfo)
                                        .build();
                            }
                        }
                    }
                }
            }

            throw new RuntimeException("분석할 가공 데이터가 없습니다.");

        } catch (Exception e) {
            throw new RuntimeException("JSON 데이터 분석 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 홀 정보 처리
     */
    private void processHole(JsonNode hole, Map<Integer, Set<String>> faceMachiningData,
                             Map<Integer, String> faceTypeMap, Map<Integer, Double> faceDiameterMap,
                             Map<Integer, Double> faceAreaMap, Set<Integer> processedFaceIds) {

        double diameter = 0;
        if (hole.has("bores") && hole.get("bores").size() > 0 &&
                hole.get("bores").get(0).has("diameter")) {
            diameter = hole.get("bores").get(0).get("diameter").asDouble();
        }

        // 면 ID 처리
        if (hole.has("faceIds")) {
            for (JsonNode faceIdNode : hole.get("faceIds")) {
                int faceId = faceIdNode.asInt();
                processedFaceIds.add(faceId);

                if (!faceMachiningData.containsKey(faceId)) {
                    faceMachiningData.put(faceId, new HashSet<>());
                }
                faceTypeMap.put(faceId, "hole");
                faceDiameterMap.put(faceId, diameter);
            }
        }

        // 보어 처리
        if (hole.has("bores")) {
            for (JsonNode bore : hole.get("bores")) {
                if (bore.has("faceIds") && bore.has("axis")) {
                    double[] axis = getDoubleArrayFromJsonNode(bore.get("axis"));

                    for (JsonNode faceIdNode : bore.get("faceIds")) {
                        int faceId = faceIdNode.asInt();

                        if (!faceMachiningData.containsKey(faceId)) {
                            faceMachiningData.put(faceId, new HashSet<>());
                        }

                        faceMachiningData.get(faceId).add(getAxisKey(axis));
                        faceMachiningData.get(faceId).add(getAxisKey(getOppositeAxis(axis)));
                        faceTypeMap.put(faceId, "hole");
                        faceDiameterMap.put(faceId, diameter);

                        if (bore.has("area")) {
                            faceAreaMap.put(faceId, bore.get("area").asDouble());
                        }
                    }
                }
            }
        }

        // 바닥면 처리
        if (hole.has("bottom") && hole.get("bottom").has("faceIds") &&
                hole.get("bottom").has("axis")) {

            double[] axis = getDoubleArrayFromJsonNode(hole.get("bottom").get("axis"));

            for (JsonNode faceIdNode : hole.get("bottom").get("faceIds")) {
                int faceId = faceIdNode.asInt();

                if (!faceMachiningData.containsKey(faceId)) {
                    faceMachiningData.put(faceId, new HashSet<>());
                }

                faceMachiningData.get(faceId).add(getAxisKey(axis));
                faceMachiningData.get(faceId).add(getAxisKey(getOppositeAxis(axis)));
                faceTypeMap.put(faceId, "hole");
                faceDiameterMap.put(faceId, diameter);

                if (hole.get("bottom").has("area")) {
                    faceAreaMap.put(faceId, hole.get("bottom").get("area").asDouble());
                }
            }
        }
    }

    /**
     * 샤프트 정보 처리
     */
    private void processShaft(JsonNode shaft, Map<Integer, Set<String>> faceMachiningData,
                              Map<Integer, String> faceTypeMap, Map<Integer, Double> faceDiameterMap,
                              Map<Integer, Double> faceAreaMap, Set<Integer> processedFaceIds) {

        if (shaft.has("faceIds") && shaft.has("axis")) {
            double[] axis = getDoubleArrayFromJsonNode(shaft.get("axis"));
            double diameter = shaft.has("diameter") ? shaft.get("diameter").asDouble() : 0;

            for (JsonNode faceIdNode : shaft.get("faceIds")) {
                int faceId = faceIdNode.asInt();
                processedFaceIds.add(faceId);

                if (!faceMachiningData.containsKey(faceId)) {
                    faceMachiningData.put(faceId, new HashSet<>());
                }

                faceMachiningData.get(faceId).add(getAxisKey(axis));
                faceTypeMap.put(faceId, "shaft");
                faceDiameterMap.put(faceId, diameter);

                if (shaft.has("area")) {
                    faceAreaMap.put(faceId, shaft.get("area").asDouble());
                }
            }
        }
    }

    /**
     * 직경 계산
     */
    private double calculateDiameter(JsonNode face) {
        if (face.has("diameter")) {
            return face.get("diameter").asDouble();
        } else if (face.has("surfaceType") && "cylinder".equals(face.get("surfaceType").asText()) &&
                face.has("maxConcaveCurvature") && face.get("maxConcaveCurvature").asDouble() != 0) {
            // maxConcaveCurvature의 역수는 반지름이므로 2를 곱해 직경 계산
            return 2 * (1 / face.get("maxConcaveCurvature").asDouble());
        }
        return 0;
    }

    /**
     * 3축 가공면 처리 - 발견된 실린더 수를 반환
     * @return 발견된 실린더 수
     */
    private int processMilled3AxesFace(JsonNode face, Map<Integer, Set<String>> faceMachiningData,
                                       Map<Integer, String> faceTypeMap, Map<Integer, Double> faceDiameterMap,
                                       Map<Integer, Double> faceAreaMap, Set<Integer> threeDMachiningFaces,
                                       Map<Integer, CylinderSideMillingData> cylinderSideMilling,
                                       List<PlaneWithZeroDepth> planesWithZeroDepth,
                                       Map<Integer, List<double[]>> planeEndMillingAxes) {

        if (!face.has("faceId")) {
            return 0;
        }

        int faceId = face.get("faceId").asInt();
        int cylinderCount = 0; // 발견된 실린더 수를 카운트

        if (!faceMachiningData.containsKey(faceId)) {
            faceMachiningData.put(faceId, new HashSet<>());
        }

        if (face.has("area")) {
            faceAreaMap.put(faceId, face.get("area").asDouble());
        }

        double calculatedDiameter = calculateDiameter(face);
        if (calculatedDiameter > 0) {
            faceDiameterMap.put(faceId, calculatedDiameter);
        }

        // 3D 가공이 필요한 특수 표면 타입 확인
        if (face.has("surfaceType")) {
            String surfaceType = face.get("surfaceType").asText();

            if ("torus".equals(surfaceType) || "b-surface".equals(surfaceType) || "sphere".equals(surfaceType)) {
                threeDMachiningFaces.add(faceId);
                return 0;
            }

            // 평면 표면 처리
            if ("plane".equals(surfaceType)) {
                boolean hasAllZeroDepths = face.has("pointDepths") && face.get("pointDepths").size() > 0;

                if (hasAllZeroDepths) {
                    // 모든 깊이가 0에 가까운지 확인
                    boolean allZero = true;
                    for (JsonNode depth : face.get("pointDepths")) {
                        if (Math.abs(depth.asDouble()) >= 0.001) {
                            allZero = false;
                            break;
                        }
                    }

                    if (allZero && face.has("millingAxes")) {
                        List<double[]> standardSideMillingAxes = new ArrayList<>();
                        List<double[]> endMillingAxes = new ArrayList<>();

                        for (JsonNode millingAxis : face.get("millingAxes")) {
                            if (millingAxis.has("isMachinable") && millingAxis.get("isMachinable").asBoolean() &&
                                    millingAxis.has("isAccessible") && millingAxis.get("isAccessible").asBoolean() &&
                                    millingAxis.has("millingAxis") && millingAxis.has("type")) {

                                double[] axisVector = getDoubleArrayFromJsonNode(millingAxis.get("millingAxis"));
                                String type = millingAxis.get("type").asText();

                                if ("sideMilling".equals(type) && isStandardAxis(axisVector)) {
                                    standardSideMillingAxes.add(axisVector);
                                }

                                if ("endMilling".equals(type)) {
                                    endMillingAxes.add(axisVector);
                                }
                            }
                        }

                        if (!standardSideMillingAxes.isEmpty()) {
                            PlaneWithZeroDepth plane = PlaneWithZeroDepth.builder()
                                    .faceId(faceId)
                                    .standardSideMillingAxes(standardSideMillingAxes)
                                    .endMillingAxes(endMillingAxes)
                                    .build();

                            planesWithZeroDepth.add(plane);
                            planeEndMillingAxes.put(faceId, endMillingAxes);
                        }
                    }
                }
            }

            // 실린더 표면 처리
            if ("cylinder".equals(surfaceType)) {
                double cylinderDiameter = calculateDiameter(face);

                if (cylinderDiameter > 0) {
                    faceDiameterMap.put(faceId, cylinderDiameter);
                }

                CylinderSideMillingData cylinderData = new CylinderSideMillingData();
                cylinderData.setDiameter(cylinderDiameter);
                cylinderSideMilling.put(faceId, cylinderData);

                boolean onlyNonMachinableOrNonAccessible = true;
                boolean allSideMillingImpossible = true;

                if (face.has("millingAxes")) {
                    for (JsonNode millingAxis : face.get("millingAxes")) {
                        if (millingAxis.has("isMachinable") && millingAxis.get("isMachinable").asBoolean() &&
                                millingAxis.has("isAccessible") && millingAxis.get("isAccessible").asBoolean()) {

                            onlyNonMachinableOrNonAccessible = false;

                            if (millingAxis.has("type") && "sideMilling".equals(millingAxis.get("type").asText()) &&
                                    millingAxis.has("millingAxis")) {

                                allSideMillingImpossible = false;
                                double[] axis = getDoubleArrayFromJsonNode(millingAxis.get("millingAxis"));

                                if (isStandardAxis(axis)) {
                                    cylinderData.getStandardAxes().add(getAxisKey(axis));
                                } else {
                                    cylinderData.getNonStandardAxes().add(getAxisKey(axis));
                                }
                            }
                        }
                    }
                }

                if (onlyNonMachinableOrNonAccessible || allSideMillingImpossible) {
                    threeDMachiningFaces.add(faceId);
                    return 0;
                }

                if (!faceTypeMap.containsKey(faceId) && !cylinderData.getStandardAxes().isEmpty()) {
                    cylinderCount++; // 실린더 카운트 증가
                    faceTypeMap.put(faceId, "cylinder");
                    faceDiameterMap.put(faceId, cylinderDiameter);
                }
            }
        }

        // 가공 축 처리
        if (face.has("millingAxes")) {
            for (JsonNode millingAxis : face.get("millingAxes")) {
                if (millingAxis.has("isMachinable") && millingAxis.get("isMachinable").asBoolean() &&
                        millingAxis.has("isAccessible") && millingAxis.get("isAccessible").asBoolean() &&
                        millingAxis.has("millingAxis")) {

                    double[] axis = getDoubleArrayFromJsonNode(millingAxis.get("millingAxis"));
                    faceMachiningData.get(faceId).add(getAxisKey(axis));

                    if (!faceTypeMap.containsKey(faceId)) {
                        faceTypeMap.put(faceId, "face");
                    }
                }
            }
        }

        // 가공 축이 추가되지 않았으면 3D 가공 면으로 표시
        if (faceMachiningData.get(faceId).isEmpty()) {
            threeDMachiningFaces.add(faceId);
        }

        return cylinderCount; // 발견된 실린더 수 반환
    }

    /**
     * 축-면 매핑 생성
     */
    private Map<String, AxisFaceGroup> createAxisFaceMap(Map<Integer, Set<String>> faceMachiningData,
                                                         Map<Integer, String> faceTypeMap,
                                                         Set<Integer> threeDMachiningFaces) {
        Map<String, AxisFaceGroup> axisFaceMap = new HashMap<>();

        for (Map.Entry<Integer, Set<String>> entry : faceMachiningData.entrySet()) {
            int faceId = entry.getKey();

            if (threeDMachiningFaces.contains(faceId)) {
                continue;
            }

            for (String axisKey : entry.getValue()) {
                if (!axisFaceMap.containsKey(axisKey)) {
                    axisFaceMap.put(axisKey, new AxisFaceGroup());
                }

                axisFaceMap.get(axisKey).getFaces().add(faceId);

                if ("hole".equals(faceTypeMap.get(faceId)) || "shaft".equals(faceTypeMap.get(faceId))) {
                    axisFaceMap.get(axisKey).incrementHoleShaftCount();
                } else {
                    axisFaceMap.get(axisKey).incrementFaceCount();
                }
            }
        }

        return axisFaceMap;
    }

    /**
     * 우선순위별로 축 정렬
     */
    private List<String> sortAxesByPriority(Map<String, AxisFaceGroup> axisFaceMap) {
        List<String> sortedAxes = new ArrayList<>(axisFaceMap.keySet());

        sortedAxes.sort((a, b) -> {
            AxisFaceGroup groupA = axisFaceMap.get(a);
            AxisFaceGroup groupB = axisFaceMap.get(b);

            if (groupB.getHoleShaftCount() != groupA.getHoleShaftCount()) {
                return groupB.getHoleShaftCount() - groupA.getHoleShaftCount();
            }

            return groupB.getFaceCount() - groupA.getFaceCount();
        });

        return sortedAxes;
    }

    /**
     * 최적 축 찾기
     */
    private List<double[]> findOptimalAxes(List<String> sortedAxes,
                                           Map<String, AxisFaceGroup> axisFaceMap,
                                           Map<Integer, Set<String>> faceMachiningData,
                                           Map<Integer, String> faceTypeMap) {
        List<double[]> optimalAxes = new ArrayList<>();
        Set<Integer> remainingFaces = new HashSet<>();

        // 모든 면을 남은 면으로 추가
        for (Map.Entry<Integer, Set<String>> entry : faceMachiningData.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                remainingFaces.add(entry.getKey());
            }
        }

        while (!remainingFaces.isEmpty() && !sortedAxes.isEmpty()) {
            String topAxisKey = sortedAxes.get(0);
            double[] topAxis = getDoubleArrayFromString(topAxisKey);

            optimalAxes.add(topAxis);

            // 이 축으로 처리할 수 있는 면 제거
            for (Integer faceId : axisFaceMap.get(topAxisKey).getFaces()) {
                remainingFaces.remove(faceId);
            }

            // 첫 번째 축이면 반대쪽 축도 추가
            if (optimalAxes.size() == 1) {
                String oppositeAxisKey = getAxisKey(getOppositeAxis(topAxis));

                if (axisFaceMap.containsKey(oppositeAxisKey) &&
                        !axisFaceMap.get(oppositeAxisKey).getFaces().isEmpty()) {

                    optimalAxes.add(getOppositeAxis(topAxis));

                    // 반대 축으로 처리할 수 있는 면 제거
                    for (Integer faceId : axisFaceMap.get(oppositeAxisKey).getFaces()) {
                        remainingFaces.remove(faceId);
                    }
                }
            }

            if (remainingFaces.isEmpty()) {
                break;
            }

            // 남은 면에 대해 축-면 매핑 재계산
            Map<String, AxisFaceGroup> newAxisFaceMap = new HashMap<>();
            for (Integer faceId : remainingFaces) {
                for (String axisKey : faceMachiningData.get(faceId)) {
                    if (!newAxisFaceMap.containsKey(axisKey)) {
                        newAxisFaceMap.put(axisKey, new AxisFaceGroup());
                    }

                    newAxisFaceMap.get(axisKey).getFaces().add(faceId);

                    if ("hole".equals(faceTypeMap.get(faceId)) || "shaft".equals(faceTypeMap.get(faceId))) {
                        newAxisFaceMap.get(axisKey).incrementHoleShaftCount();
                    } else {
                        newAxisFaceMap.get(axisKey).incrementFaceCount();
                    }
                }
            }

            // 축 우선순위 재정렬
            axisFaceMap = newAxisFaceMap;
            sortedAxes = sortAxesByPriority(axisFaceMap);
        }

        return optimalAxes;
    }

    /**
     * 특수 케이스에 대한 실린더 처리
     */
    private void processCylindersForSpecialCases(Map<Integer, CylinderSideMillingData> cylinderSideMilling,
                                                 Set<Integer> threeDMachiningFaces,
                                                 Set<Integer> specialJigFaces,
                                                 List<double[]> optimalAxes) {

        List<double[]> standardOptimalAxes = optimalAxes.stream()
                .filter(this::isStandardAxis)
                .collect(Collectors.toList());

        List<double[]> nonStandardOptimalAxes = optimalAxes.stream()
                .filter(axis -> !isStandardAxis(axis))
                .collect(Collectors.toList());

        for (Map.Entry<Integer, CylinderSideMillingData> entry : cylinderSideMilling.entrySet()) {
            int faceId = entry.getKey();
            CylinderSideMillingData cylinderData = entry.getValue();

            if (threeDMachiningFaces.contains(faceId)) {
                continue;
            }

            boolean canBeMachinedWithStandardAxis = false;

            for (String stdAxisKey : cylinderData.getStandardAxes()) {
                double[] stdAxis = getDoubleArrayFromString(stdAxisKey);
                if (standardOptimalAxes.stream().anyMatch(axis -> areAxesSame(axis, stdAxis))) {
                    canBeMachinedWithStandardAxis = true;
                    break;
                }
            }

            if (!canBeMachinedWithStandardAxis) {
                boolean onlyNonStandardAxis = !cylinderData.getNonStandardAxes().isEmpty();

                if (onlyNonStandardAxis) {
                    boolean canBeMachinedWithRecommendedNonStandardAxis = false;

                    for (String nonStdAxisKey : cylinderData.getNonStandardAxes()) {
                        double[] nonStdAxis = getDoubleArrayFromString(nonStdAxisKey);
                        if (nonStandardOptimalAxes.stream().anyMatch(axis -> areAxesSame(axis, nonStdAxis))) {
                            canBeMachinedWithRecommendedNonStandardAxis = true;
                            break;
                        }
                    }

                    if (canBeMachinedWithRecommendedNonStandardAxis) {
                        specialJigFaces.add(faceId);
                    } else {
                        threeDMachiningFaces.add(faceId);
                    }
                } else {
                    threeDMachiningFaces.add(faceId);
                }
            }
        }
    }

    /**
     * 특수 지그가 필요한 면 확인
     */
    private void checkFacesForSpecialJigs(Map<Integer, Set<String>> faceMachiningData,
                                          Set<Integer> threeDMachiningFaces,
                                          Set<Integer> specialJigFaces,
                                          List<double[]> optimalAxes) {

        List<double[]> nonStandardOptimalAxes = optimalAxes.stream()
                .filter(axis -> !isStandardAxis(axis))
                .collect(Collectors.toList());

        for (Map.Entry<Integer, Set<String>> entry : faceMachiningData.entrySet()) {
            int faceId = entry.getKey();
            Set<String> axes = entry.getValue();

            if (threeDMachiningFaces.contains(faceId) || specialJigFaces.contains(faceId)) {
                continue;
            }

            boolean hasStandardAxis = false;
            boolean hasRecommendedNonStandardAxis = false;

            for (String axisKey : axes) {
                double[] axis = getDoubleArrayFromString(axisKey);

                if (isStandardAxis(axis)) {
                    hasStandardAxis = true;
                    break;
                } else {
                    if (nonStandardOptimalAxes.stream().anyMatch(optAxis -> areAxesSame(optAxis, axis))) {
                        hasRecommendedNonStandardAxis = true;
                    }
                }
            }

            if (!hasStandardAxis && hasRecommendedNonStandardAxis) {
                specialJigFaces.add(faceId);
            }
        }
    }

    /**
     * 축별로 면 정리
     */
    private Map<String, List<FaceData>> organizeFacesByAxis(List<double[]> optimalAxes,
                                                            Map<Integer, Set<String>> faceMachiningData,
                                                            Map<Integer, String> faceTypeMap,
                                                            Map<Integer, Double> faceDiameterMap,
                                                            Set<Integer> threeDMachiningFaces,
                                                            Set<Integer> specialJigFaces) {

        Map<String, List<FaceData>> facesByAxis = new HashMap<>();

        // 각 축별 리스트 초기화
        for (double[] axis : optimalAxes) {
            String axisKey = getAxisKey(axis);
            facesByAxis.put(axisKey, new ArrayList<>());
        }

        // 먼저 홀, 샤프트, 실린더 처리
        for (Map.Entry<Integer, Set<String>> entry : faceMachiningData.entrySet()) {
            int faceId = entry.getKey();
            Set<String> axes = entry.getValue();
            String type = faceTypeMap.get(faceId);
            double diameter = faceDiameterMap.getOrDefault(faceId, 0.0);

            if (!"hole".equals(type) && !"shaft".equals(type) && !"cylinder".equals(type)) {
                continue;
            }

            if (threeDMachiningFaces.contains(faceId) || specialJigFaces.contains(faceId)) {
                continue;
            }

            List<String> compatibleAxisKeys = new ArrayList<>();

            for (String axisKey : axes) {
                double[] axis = getDoubleArrayFromString(axisKey);
                if (optimalAxes.stream().anyMatch(a -> areAxesSame(a, axis))) {
                    compatibleAxisKeys.add(axisKey);
                }
            }

            List<AxisPair> eligibleAxisPairs = new ArrayList<>();

            for (String axisKey : compatibleAxisKeys) {
                double[] axis = getDoubleArrayFromString(axisKey);
                String oppositeAxisKey = getAxisKey(getOppositeAxis(axis));

                if (optimalAxes.stream().anyMatch(a -> areAxesSame(a, axis)) &&
                        optimalAxes.stream().anyMatch(a -> areAxesSame(a, getOppositeAxis(axis)))) {

                    AxisPair pair = new AxisPair();
                    pair.setAxis1(axisKey);
                    pair.setAxis2(oppositeAxisKey);
                    eligibleAxisPairs.add(pair);
                }
            }

            if (!eligibleAxisPairs.isEmpty()) {
                AxisPair bestPair = null;
                double bestMinDiameter = -1;

                for (AxisPair pair : eligibleAxisPairs) {
                    double minDiameter1 = calculateMinDiameter(facesByAxis.get(pair.getAxis1()));
                    double minDiameter2 = calculateMinDiameter(facesByAxis.get(pair.getAxis2()));

                    double newMinDiameter1 = Math.min(minDiameter1 == Double.POSITIVE_INFINITY ?
                            Double.POSITIVE_INFINITY : minDiameter1, diameter);
                    double newMinDiameter2 = Math.min(minDiameter2 == Double.POSITIVE_INFINITY ?
                            Double.POSITIVE_INFINITY : minDiameter2, diameter);

                    // 최적의 축 선택: 직경이 가장 큰 축 선택 (최소 직경의 최대값)
                    double maxOfNewMin = Math.max(newMinDiameter1, newMinDiameter2);
                    if (maxOfNewMin > bestMinDiameter) {
                        bestMinDiameter = maxOfNewMin;
                        bestPair = new AxisPair();
                        bestPair.setAxis(newMinDiameter1 >= newMinDiameter2 ? pair.getAxis1() : pair.getAxis2());
                        bestPair.setDiameter(diameter);
                    }
                }

                if (bestPair != null) {
                    FaceData faceData = FaceData.builder()
                            .id(faceId)
                            .type(type)
                            .diameter(diameter)
                            .build();

                    facesByAxis.get(bestPair.getAxis()).add(faceData);
                }
            } else {
                for (String axisKey : compatibleAxisKeys) {
                    if (facesByAxis.containsKey(axisKey)) {
                        FaceData faceData = FaceData.builder()
                                .id(faceId)
                                .type(type)
                                .diameter(diameter)
                                .build();

                        facesByAxis.get(axisKey).add(faceData);
                    }
                }
            }
        }

        // 그 다음 다른 면 타입 처리
        for (Map.Entry<Integer, Set<String>> entry : faceMachiningData.entrySet()) {
            int faceId = entry.getKey();
            Set<String> axes = entry.getValue();
            String type = faceTypeMap.get(faceId);
            double diameter = faceDiameterMap.getOrDefault(faceId, 0.0);

            if ("hole".equals(type) || "shaft".equals(type) || "cylinder".equals(type)) {
                continue;
            }

            if (threeDMachiningFaces.contains(faceId) || specialJigFaces.contains(faceId)) {
                continue;
            }

            for (String axisKey : axes) {
                if (facesByAxis.containsKey(axisKey)) {
                    FaceData faceData = FaceData.builder()
                            .id(faceId)
                            .type(type)
                            .diameter(diameter)
                            .build();

                    facesByAxis.get(axisKey).add(faceData);
                }
            }
        }

        return facesByAxis;
    }

    /**
     * 최소 직경 계산
     */
    private double calculateMinDiameter(List<FaceData> faces) {
        if (faces == null || faces.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }

        double minDiameter = Double.POSITIVE_INFINITY;
        for (FaceData face : faces) {
            if (face.getDiameter() > 0 && face.getDiameter() < minDiameter) {
                minDiameter = face.getDiameter();
            }
        }

        return minDiameter == Double.POSITIVE_INFINITY ? 0 : minDiameter;
    }

    /**
     * 축별 최소 직경 계산
     */
    private Map<String, Double> calculateAxisMinDiameters(Map<String, List<FaceData>> facesByAxis) {
        Map<String, Double> axisMinDiameters = new HashMap<>();

        for (Map.Entry<String, List<FaceData>> entry : facesByAxis.entrySet()) {
            String axisKey = entry.getKey();
            List<FaceData> faces = entry.getValue();

            List<FaceData> holesAndShafts = faces.stream()
                    .filter(face -> "hole".equals(face.getType()) ||
                            "shaft".equals(face.getType()) ||
                            "cylinder".equals(face.getType()))
                    .collect(Collectors.toList());

            axisMinDiameters.put(axisKey, calculateMinDiameter(holesAndShafts));
        }

        return axisMinDiameters;
    }

    /**
     * 지그 요구사항 결정
     */
    private Map<String, Boolean> determineJigRequirements(List<double[]> optimalAxes,
                                                          List<PlaneWithZeroDepth> planesWithZeroDepth) {
        Map<String, Boolean> jigRequirementsByAxis = new HashMap<>();

        for (double[] axis : optimalAxes) {
            String axisKey = getAxisKey(axis);
            String oppositeAxisKey = getAxisKey(getOppositeAxis(axis));

            // 이미 처리된 경우 건너뛰기
            if (jigRequirementsByAxis.containsKey(axisKey) || jigRequirementsByAxis.containsKey(oppositeAxisKey)) {
                if (!jigRequirementsByAxis.containsKey(axisKey) && jigRequirementsByAxis.containsKey(oppositeAxisKey)) {
                    jigRequirementsByAxis.put(axisKey, jigRequirementsByAxis.get(oppositeAxisKey));
                }
                continue;
            }

            // 표준 축만 지그가 필요함
            if (!isStandardAxis(axis)) {
                jigRequirementsByAxis.put(axisKey, true);
                continue;
            }

            // sideMilling 기준으로 판단하도록 수정
            int oppositeSideMillingPairsCount = 0;

            for (int i = 0; i < planesWithZeroDepth.size(); i++) {
                PlaneWithZeroDepth plane1 = planesWithZeroDepth.get(i);
                if (plane1.getStandardSideMillingAxes() == null || plane1.getStandardSideMillingAxes().isEmpty()) {
                    continue;
                }

                for (int j = i + 1; j < planesWithZeroDepth.size(); j++) {
                    PlaneWithZeroDepth plane2 = planesWithZeroDepth.get(j);
                    if (plane2.getStandardSideMillingAxes() == null || plane2.getStandardSideMillingAxes().isEmpty()) {
                        continue;
                    }

                    for (double[] sideAxis1 : plane1.getStandardSideMillingAxes()) {
                        for (double[] sideAxis2 : plane2.getStandardSideMillingAxes()) {
                            if (areAxesOpposite(sideAxis1, sideAxis2)) {
                                String sideAxis1Key = getAxisKey(sideAxis1);
                                String sideAxis2Key = getAxisKey(sideAxis2);

                                if (areAxesSame(axis, sideAxis1) ||
                                        areAxesSame(axis, sideAxis2) ||
                                        areAxesSame(getOppositeAxis(axis), sideAxis1) ||
                                        areAxesSame(getOppositeAxis(axis), sideAxis2)) {

                                    oppositeSideMillingPairsCount++;
                                }
                            }
                        }
                    }
                }
            }

            // 반대 방향의 sideMilling 축 쌍이 없으면 지그가 필요함
            boolean jigNeeded = oppositeSideMillingPairsCount < 1;
            jigRequirementsByAxis.put(axisKey, jigNeeded);
            jigRequirementsByAxis.put(oppositeAxisKey, jigNeeded);
        }

        return jigRequirementsByAxis;
    }

    /**
     * 필요한 총 지그 수 계산
     */
    private int calculateTotalJigsNeeded(List<double[]> optimalAxes, Map<String, Boolean> jigRequirementsByAxis) {
        int totalJigsNeeded = 0;
        Set<String> processedAxisPairs = new HashSet<>();

        for (double[] axis : optimalAxes) {
            String axisKey = getAxisKey(axis);
            String oppositeAxisKey = getAxisKey(getOppositeAxis(axis));

            if (processedAxisPairs.contains(axisKey) || processedAxisPairs.contains(oppositeAxisKey)) {
                continue;
            }

            processedAxisPairs.add(axisKey);
            processedAxisPairs.add(oppositeAxisKey);

            if (jigRequirementsByAxis.containsKey(axisKey) && jigRequirementsByAxis.get(axisKey)) {
                totalJigsNeeded++;
            }
        }

        return totalJigsNeeded;
    }

    /**
     * 필요한 표준 지그 수 계산
     */
    private int calculateStandardJigsNeeded(List<double[]> optimalAxes, Map<String, Boolean> jigRequirementsByAxis) {
        int standardJigsNeeded = 0;
        Set<String> processedAxisPairs = new HashSet<>();

        for (double[] axis : optimalAxes) {
            if (!isStandardAxis(axis)) {
                continue;
            }

            String axisKey = getAxisKey(axis);
            String oppositeAxisKey = getAxisKey(getOppositeAxis(axis));

            if (processedAxisPairs.contains(axisKey) || processedAxisPairs.contains(oppositeAxisKey)) {
                continue;
            }

            processedAxisPairs.add(axisKey);
            processedAxisPairs.add(oppositeAxisKey);

            if (jigRequirementsByAxis.containsKey(axisKey) && jigRequirementsByAxis.get(axisKey)) {
                standardJigsNeeded++;
            }
        }

        return standardJigsNeeded;
    }

    /**
     * 특수 면의 면적 계산
     */
    private double calculateSpecialFaceArea(Set<Integer> threeDMachiningFaces,
                                            Set<Integer> specialJigFaces,
                                            Map<Integer, Double> faceAreaMap) {
        double totalSpecialArea = 0;
        Set<Integer> allSpecialFaces = new HashSet<>();
        allSpecialFaces.addAll(threeDMachiningFaces);
        allSpecialFaces.addAll(specialJigFaces);

        for (Integer faceId : allSpecialFaces) {
            if (faceAreaMap.containsKey(faceId)) {
                totalSpecialArea += faceAreaMap.get(faceId);
            }
        }

        return totalSpecialArea;
    }

    /**
     * 축 우선순위 정보 리스트 생성
     */
    private List<AxisPriorityInfo> createAxisPriorityInfoList(List<String> sortedAxes,
                                                              Map<String, AxisFaceGroup> axisFaceMap) {
        List<AxisPriorityInfo> axisPriorityInfoList = new ArrayList<>();

        int maxDisplay = Math.min(sortedAxes.size(), 15);
        for (int i = 0; i < maxDisplay; i++) {
            String axisKey = sortedAxes.get(i);
            double[] axis = getDoubleArrayFromString(axisKey);

            AxisPriorityInfo info = AxisPriorityInfo.builder()
                    .axis(axis)
                    .holeShaftCount(axisFaceMap.get(axisKey).getHoleShaftCount())
                    .faceCount(axisFaceMap.get(axisKey).getFaceCount())
                    .build();

            axisPriorityInfoList.add(info);
        }

        return axisPriorityInfoList;
    }

    /**
     * 면 세부 정보 리스트 생성
     */
    private List<FaceDetailsInfo> createFaceDetailsInfoList(List<double[]> optimalAxes,
                                                            Map<String, List<FaceData>> facesByAxis) {
        List<FaceDetailsInfo> faceDetailsInfoList = new ArrayList<>();

        for (double[] axis : optimalAxes) {
            String axisKey = getAxisKey(axis);
            List<FaceData> faces = facesByAxis.get(axisKey);

            // ID로 면 정렬
            faces.sort(Comparator.comparing(FaceData::getId));

            FaceDetailsInfo info = FaceDetailsInfo.builder()
                    .axis(axis)
                    .faces(faces)
                    .build();

            faceDetailsInfoList.add(info);
        }

        return faceDetailsInfoList;
    }

    /**
     * 축별 최소 직경 정보 리스트 생성
     */
    private List<AxisMinDiameterInfo> createAxisMinDiameterInfoList(List<double[]> optimalAxes,
                                                                    Map<String, Double> axisMinDiameters) {
        List<AxisMinDiameterInfo> axisMinDiameterInfoList = new ArrayList<>();

        for (double[] axis : optimalAxes) {
            String axisKey = getAxisKey(axis);
            double minDiameter = axisMinDiameters.getOrDefault(axisKey, 0.0);

            AxisMinDiameterInfo info = AxisMinDiameterInfo.builder()
                    .axis(axis)
                    .minDiameter(minDiameter)
                    .build();

            axisMinDiameterInfoList.add(info);
        }

        return axisMinDiameterInfoList;
    }

    /**
     * 지그 요구사항 정보 리스트 생성
     */
    private List<JigRequirementInfo> createJigRequirementInfoList(List<double[]> optimalAxes,
                                                                  Map<String, Boolean> jigRequirementsByAxis) {
        List<JigRequirementInfo> jigRequirementInfoList = new ArrayList<>();
        Set<String> processedAxisPairs = new HashSet<>();

        for (double[] axis : optimalAxes) {
            if (!isStandardAxis(axis)) {
                continue;
            }

            String axisKey = getAxisKey(axis);
            String oppositeAxisKey = getAxisKey(getOppositeAxis(axis));

            if (processedAxisPairs.contains(axisKey) || processedAxisPairs.contains(oppositeAxisKey)) {
                continue;
            }

            processedAxisPairs.add(axisKey);
            processedAxisPairs.add(oppositeAxisKey);

            boolean hasOppositeAxis = optimalAxes.stream()
                    .anyMatch(a -> areAxesSame(a, getOppositeAxis(axis)));

            String axisDisplay = hasOppositeAxis ?
                    formatAxis(axis) + " / " + formatAxis(getOppositeAxis(axis)) :
                    formatAxis(axis);

            boolean jigNeeded = jigRequirementsByAxis.getOrDefault(axisKey, true);

            JigRequirementInfo info = JigRequirementInfo.builder()
                    .axisDisplay(axisDisplay)
                    .jigNeeded(jigNeeded)
                    .build();

            jigRequirementInfoList.add(info);
        }

        return jigRequirementInfoList;
    }

    /**
     * 최적 축 정보 리스트 생성
     */
    private List<OptimalAxisInfo> createOptimalAxisInfoList(List<double[]> optimalAxes) {
        List<OptimalAxisInfo> optimalAxisInfoList = new ArrayList<>();

        for (double[] axis : optimalAxes) {
            OptimalAxisInfo info = OptimalAxisInfo.builder()
                    .axis(axis)
                    .isStandardAxis(isStandardAxis(axis))
                    .build();

            optimalAxisInfoList.add(info);
        }

        return optimalAxisInfoList;
    }

    // 유틸리티 메소드

    /**
     * 축의 키 생성
     */
    private String getAxisKey(double[] axis) {
        return String.format("%.3f,%.3f,%.3f", roundNumber(axis[0]), roundNumber(axis[1]), roundNumber(axis[2]));
    }

    /**
     * 반대 축 얻기
     */
    private double[] getOppositeAxis(double[] axis) {
        return new double[] {-axis[0], -axis[1], -axis[2]};
    }

    /**
     * 두 축이 같은지 확인
     */
    private boolean areAxesSame(double[] axis1, double[] axis2) {
        if (axis1 == null || axis2 == null) {
            return false;
        }

        if (axis1.length != axis2.length) {
            return false;
        }

        for (int i = 0; i < axis1.length; i++) {
            double a1 = roundNumber(axis1[i]);
            double a2 = roundNumber(axis2[i]);

            if (Math.abs(a1 - a2) > 0.001) {
                return false;
            }
        }

        return true;
    }

    /**
     * 두 축이 반대인지 확인
     */
    private boolean areAxesOpposite(double[] axis1, double[] axis2) {
        if (axis1 == null || axis2 == null) {
            return false;
        }

        if (axis1.length != axis2.length) {
            return false;
        }

        return areAxesSame(axis2, getOppositeAxis(axis1));
    }

    /**
     * 표준 축인지 확인
     */
    private boolean isStandardAxis(double[] axis) {
        if (axis == null) {
            return false;
        }

        for (double[] stdAxis : STANDARD_AXES) {
            if (areAxesSame(axis, stdAxis)) {
                return true;
            }
        }

        return false;
    }

    /**
     * JsonNode에서 double 배열 얻기
     */
    private double[] getDoubleArrayFromJsonNode(JsonNode node) {
        double[] result = new double[node.size()];
        for (int i = 0; i < node.size(); i++) {
            result[i] = node.get(i).asDouble();
        }
        return result;
    }

    /**
     * 문자열에서 double 배열 얻기
     */
    private double[] getDoubleArrayFromString(String axisKey) {
        String[] parts = axisKey.split(",");
        double[] result = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Double.parseDouble(parts[i]);
        }
        return result;
    }

    /**
     * 축 형식 지정
     */
    private static String formatAxis(double[] axis) {
        if (axis == null) {
            return "N/A";
        }
        return String.format("[%.3f, %.3f, %.3f]", roundNumber(axis[0]), roundNumber(axis[1]), roundNumber(axis[2]));
    }

    /**
     * 숫자 반올림
     */
    private static double roundNumber(double num) {
        return Math.round(num * 1000) / 1000.0;
    }

    // 내부 데이터 구조 클래스

    /**
     * 실린더 측면 가공 데이터
     */
    @Getter
    @Setter
    public static class CylinderSideMillingData {
        private Set<String> standardAxes = new HashSet<>();
        private Set<String> nonStandardAxes = new HashSet<>();
        private double diameter;
    }

    /**
     * 축-면 그룹
     */
    @Getter
    public static class AxisFaceGroup {
        private Set<Integer> faces = new HashSet<>();
        private int holeShaftCount = 0;
        private int faceCount = 0;

        public void incrementHoleShaftCount() {
            this.holeShaftCount++;
        }
        public void incrementFaceCount() {
            this.faceCount++;
        }
    }

    /**
     * 축 쌍
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AxisPair {
        private String axis1;
        private String axis2;
        private String axis;
        private double diameter;
    }

    // DTO 클래스들

    /**
     * 분석 결과 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class AnalysisResult {
        private List<OptimalAxisInfo> optimalAxes;
        private MachiningInfo machiningInfo;
        private List<AxisMinDiameterInfo> axisMinDiameters;
        private List<JigRequirementInfo> jigRequirements;
        private int standardJigsNeeded;
        private List<AxisPriorityInfo> axisPriorities;
        private List<FaceDetailsInfo> faceDetails;
        private ThreeDMachiningInfo threeDMachining;
    }

    /**
     * 최적 축 정보 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class OptimalAxisInfo {
        private double[] axis;
        private boolean isStandardAxis;
    }

    /**
     * 가공 정보 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class MachiningInfo {
        private int totalHoleCount;
        private int totalCylinderCount;
        private int totalShaftCount;
    }

    /**
     * 축별 최소 직경 정보 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class AxisMinDiameterInfo {
        private double[] axis;
        private double minDiameter;
    }

    /**
     * 지그 요구사항 정보 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class JigRequirementInfo {
        private String axisDisplay;
        private boolean jigNeeded;
    }

    /**
     * 축 우선순위 정보 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class AxisPriorityInfo {
        private double[] axis;
        private int holeShaftCount;
        private int faceCount;

    }

    /**
     * 면 세부 정보 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class FaceDetailsInfo {
        private double[] axis;
        private List<FaceData> faces;
    }

    /**
     * 면 데이터 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class FaceData {
        private int id;
        private String type;
        private double diameter;
    }

    /**
     * 3D 가공 정보 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class ThreeDMachiningInfo {
        private List<Integer> specialFaces;
        private double totalSpecialArea;
    }

    /**
     * 깊이가 0인 평면 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class PlaneWithZeroDepth {
        private final int faceId;
        private final List<double[]> standardSideMillingAxes;
        private final List<double[]> endMillingAxes;
    }

    /**
     * 분석 결과 저장 객체
     */
    @Getter
    @Builder
    @ToString
    public static class Result {
        private int standardAxisCount; // 표준 축 개수 (최적 가공 축 개수)
        private int totalProcessingCount; // 총 가공 대상 수
        private int total3DAreaCount; // 3D 가공 면적 개수
        private double total3DArea; // 3D 가공 면적
        private int totalJigCount; // 총 지그 수
        private double[] minDiameterByAxis; // 축별 최소 직경
    }


    /**
     * 분석 결과를 콘솔에 출력합니다.
     */
    public static CNCAxisAnalyzer.Result printResults(AnalysisResult result) {
        // 표준축 개수 계산
        int standardAxisCount = 0;
        for (int i = 0; i < result.getOptimalAxes().size(); i++) {
            if (result.getOptimalAxes().get(i).isStandardAxis()) {
                standardAxisCount++;
            }
        }

        // 최적 가공축 출력 (표준축 개수만 표시)
        System.out.println("\n===== 최적 가공축 조합 =====");
        System.out.println("최적 가공축 " + standardAxisCount + "개:");

        for (int i = 0; i < result.getOptimalAxes().size(); i++) {
            double[] axis = result.getOptimalAxes().get(i).getAxis();
            boolean isStandard = result.getOptimalAxes().get(i).isStandardAxis();
            System.out.printf("%d: %s (%s)\n",
                    i + 1,
                    formatAxis(axis),
                    isStandard ? "표준축" : "비표준축");
        }

        // 가공 대상 정보 출력
        System.out.println("\n===== 가공 대상 정보 =====");
        System.out.println("홀(Hole) 수: " + result.getMachiningInfo().getTotalHoleCount() + "개");
        // 실린더 갯수 디버깅 메시지 추가
        System.out.println("실린더(Cylinder) 수: " + result.getMachiningInfo().getTotalCylinderCount() + "개");
        System.out.println("샤프트(Shaft) 수: " + result.getMachiningInfo().getTotalShaftCount() + "개");
        System.out.println("총 가공 대상 수: " +
                (result.getMachiningInfo().getTotalHoleCount() +
                        result.getMachiningInfo().getTotalCylinderCount() +
                        result.getMachiningInfo().getTotalShaftCount()) + "개");

        // 축별 최소 직경 출력 (표준축만 표시)
        System.out.println("\n===== 축별 최소 홀/실린더 직경 (표준축만) =====");
        for (int i = 0; i < result.getAxisMinDiameters().size(); i++) {
            double[] axis = result.getAxisMinDiameters().get(i).getAxis();
            double minDiameter = result.getAxisMinDiameters().get(i).getMinDiameter();

            // 표준축인지 확인
            boolean isStandardAxis = false;
            for (int j = 0; j < result.getOptimalAxes().size(); j++) {
                if (areAxisSame(result.getOptimalAxes().get(j).getAxis(), axis) &&
                        result.getOptimalAxes().get(j).isStandardAxis()) {
                    isStandardAxis = true;
                    break;
                }
            }

            // 표준축만 출력
            if (isStandardAxis) {
                System.out.printf("%s: %s\n",
                        formatAxis(axis),
                        minDiameter == 0 ? "해당 없음" : roundNumber(minDiameter));
            }
        }

        // 지그 필요 여부 출력
        System.out.println("\n===== 지그 필요 여부 =====");
        for (int i = 0; i < result.getJigRequirements().size(); i++) {
            String axisDisplay = result.getJigRequirements().get(i).getAxisDisplay();
            boolean jigNeeded = result.getJigRequirements().get(i).isJigNeeded();
            System.out.printf("%s: %s\n",
                    axisDisplay,
                    jigNeeded ? "필요" : "불필요");
        }
        System.out.println("표준축용 필요 지그 수: " + result.getStandardJigsNeeded() + "개");
        System.out.println("참고: 마주보는 축(예: x+/x-)은 한 쌍으로 계산됩니다.");

        // 가공축 우선순위 출력
        System.out.println("\n===== 가공축 우선순위 랭킹 =====");
        int displayCount = Math.min(result.getAxisPriorities().size(), 15);
        for (int i = 0; i < displayCount; i++) {
            double[] axis = result.getAxisPriorities().get(i).getAxis();
            int holeShaftCount = result.getAxisPriorities().get(i).getHoleShaftCount();
            int faceCount = result.getAxisPriorities().get(i).getFaceCount();
            int totalCount = holeShaftCount + faceCount;

            System.out.printf("%d: %s (홀/샤프트: %d, 면: %d, 총합: %d)\n",
                    i + 1,
                    formatAxis(axis),
                    holeShaftCount,
                    faceCount,
                    totalCount);
        }

        // 면별 가공 세부 정보 출력
        System.out.println("\n===== 면별 가공 세부 정보 =====");
        for (int i = 0; i < result.getFaceDetails().size(); i++) {
            double[] axis = result.getFaceDetails().get(i).getAxis();
            System.out.printf("\n가공축: %s (총: %d개 면)\n",
                    formatAxis(axis),
                    result.getFaceDetails().get(i).getFaces().size());

            if (!result.getFaceDetails().get(i).getFaces().isEmpty()) {
                System.out.println("면 ID\t유형\t직경");
                System.out.println("-----------------------------");

                for (int j = 0; j < result.getFaceDetails().get(i).getFaces().size(); j++) {
                    int faceId = result.getFaceDetails().get(i).getFaces().get(j).getId();
                    String faceType = result.getFaceDetails().get(i).getFaces().get(j).getType();
                    double diameter = result.getFaceDetails().get(i).getFaces().get(j).getDiameter();

                    String displayType = "면";
                    if ("hole".equals(faceType)) displayType = "홀";
                    else if ("shaft".equals(faceType)) displayType = "샤프트";
                    else if ("cylinder".equals(faceType)) displayType = "실린더";

                    String displayDiameter = diameter > 0 ? String.valueOf(roundNumber(diameter)) : "-";

                    System.out.printf("%d\t%s\t%s\n", faceId, displayType, displayDiameter);
                }
            } else {
                System.out.println("이 축에 연결된 면이 없습니다.");
            }
        }

        // 3D 가공 정보 출력
        System.out.println("\n===== 3D 가공이 필요한 면 =====");
        if (!result.getThreeDMachining().getSpecialFaces().isEmpty()) {
            System.out.printf("면 ID (총 %d개): %s\n",
                    result.getThreeDMachining().getSpecialFaces().size(),
                    String.join(", ", result.getThreeDMachining().getSpecialFaces().stream()
                            .map(String::valueOf).toArray(String[]::new)));

            System.out.println("총 면적: " + roundNumber(result.getThreeDMachining().getTotalSpecialArea()) + " 제곱단위");
            System.out.println("\n이 면들은 다음 조건 중 하나에 해당합니다:");
            System.out.println("- 어떤 축으로도 가공이 불가능한 면");
            System.out.println("- torus, b-surface, sphere 표면 타입");
            System.out.println("- cylinder 타입이면서 모든 sideMilling이 불가능한 면");
            System.out.println("- 표준 축으로 sideMilling이 불가능한 cylinder 면");
            System.out.println("- 인식되지 않은 면 (unrecognized)");
            System.out.println("- 표준 축으로는 가공이 불가능하고, 추천된 비표준 축으로만 가공이 가능한 면 (특수 지그 필요)");
        } else {
            System.out.println("3D 가공이 필요한 면이 없습니다.");
        }

        CNCAxisAnalyzer.Result analysisResult = Result.builder()
                .standardAxisCount(standardAxisCount)
                .totalProcessingCount(result.getMachiningInfo().getTotalHoleCount() +
                        result.getMachiningInfo().getTotalCylinderCount() +
                        result.getMachiningInfo().getTotalShaftCount())
                .total3DAreaCount(result.getThreeDMachining().getSpecialFaces().size())
                .total3DArea(result.getThreeDMachining().getTotalSpecialArea())
                .totalJigCount(result.getStandardJigsNeeded())
                .minDiameterByAxis(
                        result.getAxisMinDiameters().stream()
                                .map(AxisMinDiameterInfo::getMinDiameter)
                                .map(d -> (d == 0 || d >= 6.0) ? 6.0 : d)
                                .mapToDouble(Double::doubleValue)
                                .toArray())
                .build();

        return analysisResult;
    }

    /**
     * 두 축이 같은지 비교
     */
    private static boolean areAxisSame(double[] axis1, double[] axis2) {
        if (axis1 == null || axis2 == null) {
            return false;
        }

        if (axis1.length != axis2.length) {
            return false;
        }

        for (int i = 0; i < axis1.length; i++) {
            double a1 = roundNumber(axis1[i]);
            double a2 = roundNumber(axis2[i]);

            if (Math.abs(a1 - a2) > 0.001) {
                return false;
            }
        }

        return true;
    }
}