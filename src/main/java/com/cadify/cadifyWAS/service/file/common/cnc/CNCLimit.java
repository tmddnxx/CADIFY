package com.cadify.cadifyWAS.service.file.common.cnc;

import com.cadify.cadifyWAS.model.dto.files.CostDTO;
import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.cadify.cadifyWAS.service.file.common.CommentType;
import com.cadify.cadifyWAS.service.file.common.FileCommon;
import com.cadify.cadifyWAS.service.file.common.MethodType;
import com.cadify.cadifyWAS.service.file.enumValues.cnc.defaultValue.CNCHoleSize;
import com.cadify.cadifyWAS.service.file.enumValues.cnc.defaultValue.CNCMaterialDensity;
import com.cadify.cadifyWAS.service.file.enumValues.cnc.defaultValue.CNCMaterialMachiningRate;
import com.cadify.cadifyWAS.service.file.enumValues.cnc.limitValue.CNCDrillHoleSize;
import com.cadify.cadifyWAS.service.file.enumValues.cnc.limitValue.CNCEndMillSize;
import com.cadify.cadifyWAS.service.file.enumValues.cnc.priceValue.CNCCostByHole;
import com.cadify.cadifyWAS.service.file.enumValues.cnc.priceValue.CNCCostBySurface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Stream;

@Log4j2
public class CNCLimit {

    /*----------------------------- 업로드 시 검사 -----------------------*/

    // metaJson에서 타입 찾기
    public static String extractCnCType(JsonNode parts){
        if (parts == null || !parts.isArray() || parts.size() == 0) {
            throw new IllegalArgumentException("잘못된 모델링입니다. \n다시 확인해주세요");
        }

        if (parts.size() > 1) {
            throw new RuntimeException("현재 단품만 지원하고 있습니다. \n단일 모델링을 업로드 해 주세요");
        }

        JsonNode bodies = parts.get(0).get("bodies");
        if (bodies == null || !bodies.isArray() || bodies.size() == 0) {
            throw new IllegalArgumentException("두께가 없는 surface 형상입니다. \n정상적인 Solid 형상으로 모델링 해주세요");
        }

        JsonNode firstBody = bodies.get(0);
        String type = firstBody.path("type").asText(); // path()는 null-safe
        JsonNode features = firstBody.path("features");

        boolean allEmpty = Stream.of("shafts", "holes", "milledFaces3Axes", "prismaticMilling")
                .map(features::path)
                .allMatch(node -> node.isArray() && node.isEmpty());

        return allEmpty ? "CNC_PURE_LATHE" : type;
    }


    // metaJson에서 위험코드 찾기
    public static List<Integer> extractWarningDetailsByCnc(JsonNode parts) {
        JsonNode body = parts.get(0).path("bodies").get(0);

        JsonNode semanticCodes = body.path("semanticCodes");
        List<Integer> errorCodes = new ArrayList<>();

        if (semanticCodes != null && semanticCodes.has("warnings")) {
            // semanticCodes의 warnings가 있을 경우
            JsonNode warnings = semanticCodes.path("warnings");
            for (JsonNode warning : warnings) {
                JsonNode code = warning.path("code");
                if (code.isInt()) {
                    errorCodes.add(code.asInt());
                }
            }

            return errorCodes;
        }
        // code가 없거나 parts가 없으면 null 반환
        return null;
    }

    // bbox 수치 찾기
    public static OptionDTO.BBox extractBBox(JsonNode parts) {
        JsonNode body = parts.get(0).get("bodies").get(0);
        JsonNode summary = body.path("summary");
        double bboxDx = (double) Math.round(summary.path("bboxDx").asDouble() * 100) / 100;
        double bboxDy = (double) Math.round(summary.path("bboxDy").asDouble() * 100) / 100;
        double bboxDz = (double) Math.round(summary.path("bboxDz").asDouble() * 100) / 100;

        return OptionDTO.BBox.builder()
                .x(bboxDx)
                .y(bboxDy)
                .z(bboxDz)
                .build();
    }

    // metaJson에서 hole 찾기
    public static String extractHoles(JsonNode parts, List<Estimate.ErrorDetail> errorDetailList) {
        List<OptionDTO.Hole> holeList = new ArrayList<>();
        List<List<Integer>> faceIdList2 = new ArrayList<>();
        JsonNode body = parts.get(0).get("bodies").get(0);
        JsonNode features = body.path("features");
        JsonNode holes = features.path("holes");

        int threadCount = 0;
        if (!holes.isNull() && !holes.isMissingNode()) {
            for (JsonNode hole : holes) {
                if (!hole.isMissingNode()) {
                    JsonNode threads = hole.path("threads"); // 스레드면 추가안함
                    if (!threads.isEmpty()) {
                        threadCount++;
                        continue;
                    }

                    JsonNode counterSinks = hole.path("countersinks");
                    if (counterSinks.isArray() && counterSinks.isEmpty()) continue;
                    JsonNode counterSink = counterSinks.get(0);
                    if (counterSink != null && !counterSink.isNull() &&!counterSink.isMissingNode()) {
                        JsonNode csAngle = counterSink.path("angle");
                        double angle = Math.round(csAngle.asDouble(0) * 100.0) / 100.0;
                        if (angle != 90) {
                            JsonNode faceIds = counterSink.path("faceIds");
                            Map<String, Object> data = new HashMap<>();
                            data.put("faceIds", faceIds);
                            data.put("angle", angle);
                            errorDetailList.add(
                                    Estimate.ErrorDetail.builder()
                                            .type("CNC_COUNTER_SINK")
                                            .message("카운터싱크의 각도가 90도가 아닙니다.")
                                            .data(data)
                                            .build()
                            );
                        }
                    }

                    List<Integer> faceIdList = new ArrayList<>();
                    JsonNode bores = hole.path("bores").get(0);
                    JsonNode faceIds = bores.path("faceIds");
                    if (faceIds.isArray()) {
                        for(JsonNode faceId : faceIds) {
                            faceIdList.add(faceId.asInt());
                        }
                    }
                    double diameter = (double) Math.round(bores.path("diameter").asDouble(0) * 1000) / 1000;
                    String type = CNCHoleSize.getTypeByHoleSize(diameter);

                    faceIdList2.add(faceIdList);
                    OptionDTO.Hole holeObj = OptionDTO.Hole.builder()
                            .faceIds(faceIdList2)
                            .type(type)
                            .count(1)
                            .diameter(diameter)
                            .build();

                    holeList.add(holeObj);
                }
            }
        }
        if (threadCount > 0) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("CNC_THREAD")
                            .message("탭이 실제로 모델링 되어 있어 에러가 발생했습니다. \n탭은 뷰어에서 홀을 클릭해서 설정해 주세요")
                            .build()
            );
        }

        if (holeList.isEmpty()) return null;

        return FileCommon.convertListToString(holeList);
    }

    // 업로드 시 한계치 검사
    public static String extractLimit(JsonNode parts, String type, List<Estimate.ErrorDetail> errorDetailList) {
        JsonNode body = parts.get(0).get("bodies").get(0);
        JsonNode features = body.path("features");
        if (!FileCommon.isSameMethod(type, MethodType.CNC_MILLING)) { // CNC 밀링이 아닐 경우
            Estimate.ErrorDetail errorProcessSize = handleMaxProcessingSize(features); // 최대 가공 사이즈 검사
            addList(errorProcessSize, errorDetailList);
            handleExternalGroove(features, errorDetailList); // 외경 홈 관련 한계치 검사
            handleBoreLengthDiameter(features, errorDetailList); // 선반 내경 보어 최대 길이 & 최대 직경 검사
            handleInternalGroove(features, errorDetailList); // 내경 홈 관련 한계치 검사
            handleCenterHole(features, errorDetailList); // 심압대 센터홀 검사
        } else { // CNC 밀링일 경우
            // 최대 가공 사이즈 검사
            Estimate.ErrorDetail errorProcessSizeMilling = handleMaxProcessingSizeMilling(body);
            addList(errorProcessSizeMilling, errorDetailList);
            // 밀링 포켓 모서리 최소 R값 검사
            Estimate.ErrorDetail errorPocketCorner = handlePocketCorner(features);
            addList(errorPocketCorner, errorDetailList);
            // 포켓 최대 가공 깊이 검사
            handlePocketMaxDepth(features, errorDetailList);
            // 벽 최소 간격 검사
            handleWallMinDistance(features, errorDetailList);




        }

        return FileCommon.convertListToString(errorDetailList);
    }

    /**
     * ///////////// 밀링이 아닌 경우에만 검사 /////////////
     * ///////////// 밀링이 아닌 경우에만 검사 /////////////
     */
    // CNC 밀링이 아닐 경우 최대 가공 & 최소 가공 사이즈 검사
    private static Estimate.ErrorDetail handleMaxProcessingSize(JsonNode features) {
        JsonNode maximumTurnedState = features.path("maximumTurnedState");
        if (maximumTurnedState.isMissingNode()) return null;

        double maxDiameter = (double) Math.round(maximumTurnedState.path("maxDiameter").asDouble() * 100) / 100;
        double length = (double) Math.round(maximumTurnedState.path("length").asDouble() * 100) / 100;
        
        // 최대 가공
        if (maxDiameter > 320 || length > 600) {
           return Estimate.ErrorDetail.builder()
                    .type("CNC_MAXIMUM_TURNED_STATE")
                    .message("선반 가공물의 최대 가공 사이즈는 320 PI  X 600 [mm]입니다. \n" +
                            "최대 가공 사이즈 이하로 모델링 해 주세요")
                    .data(null)
                    .build();
        }

        // 최소 가공
        if (maxDiameter < 3 || length < 3) {
            return Estimate.ErrorDetail.builder()
                    .type("CNC_MINIMUM_TURNED_STATE")
                    .message("선반 가공물의 최소 가공 사이즈는 3 PI  X 3 [mm]입니다. \n" +
                            "최소 가공 사이즈 이상으로 모델링 해 주세요")
                    .data(null)
                    .build();
        }
        
        return null;
    }

    // 외경 홈 관련 한계치
    private static void handleExternalGroove(JsonNode features, List<Estimate.ErrorDetail> errorDetailList) {
        JsonNode nvFeatures = features.path("nvFeatures");
        if (nvFeatures.isMissingNode()) return;
        JsonNode nvGrooves = nvFeatures.path("nvGrooves");
        if (nvGrooves.isEmpty() || nvGrooves.isMissingNode()) return;
        for (JsonNode groove : nvGrooves) {
            JsonNode basicPosition = groove.path("basicPosition");
            if (basicPosition == null || basicPosition.isNull() || basicPosition.isEmpty()) continue;

            boolean hasLateral = false;
            boolean hasFrontOrBack = false;

            for (JsonNode position : basicPosition) {
                if (position.asText().equals("lateral")) {
                    hasLateral = true;
                    hasFrontOrBack = false;
                    break;
                }else if (position.asText().equals("front") || position.asText().equals("back")) {
                    hasFrontOrBack = true;
                }
            }

            double maxLength = (double) Math.round(groove.path("maxLength").asDouble() * 1000) / 1000;
            double maxDiameter = (double) Math.round(groove.path("maxDiameter").asDouble() * 1000) / 1000;
            double minDiameter = (double) Math.round(groove.path("minDiameter").asDouble() * 1000) / 1000;
            double depth = (maxDiameter - minDiameter) / 2;
            if (hasLateral) {
                handleGrooveWidthDepth(maxLength, depth, errorDetailList); // 외경 홈 깊이,폭 검사
            } else if (hasFrontOrBack) {
                handleFrontOrBackGroove(maxLength, depth, errorDetailList); // 앞/뒤 홈 검사
            }
        }
    }
    
    // 외경 홈(깊이, 폭) 검사
    private static void handleGrooveWidthDepth(double maxLength, double depth, List<Estimate.ErrorDetail> errorDetailList) {
        if (maxLength < 1.15) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("CNC_EXTERNAL_GROOVE")
                            .message("현재 외경 홈 폭은 " + maxLength + "mm로 너무 작습니다. \n" +
                                    "1.15mm 이상으로 모델링해주세요.")
                            .build()
            );
        }

        if (depth > (1.5 * maxLength)) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("CNC_EXTERNAL_GROOVE")
                            .message("현재 외경 홈 깊이는 " + depth + "mm로 너무 깊습니다. \n" +
                                    "홈 깊이를 (홈 너비 x 1.5) 이하로 모델링 해 주세요.")
                            .build()
            );
        }
        if (depth > 100) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("CNC_EXTERNAL_GROOVE")
                            .message("현재 외경 홈 깊이는 " + depth + "mm로 너무 깊습니다. \n" +
                                    "홈 깊이를 100mm 이하로 모델링 해 주세요.")
                            .build()
            );
        }
    }

    // 앞/뒤 홈 관련 한계치 검사
    private static void handleFrontOrBackGroove(double maxLength, double depth, List<Estimate.ErrorDetail> errorDetailList) {
        if (maxLength > (3 * depth)) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("CNC_FRONT_BACK_GROOVE")
                            .message("축 방향 Groove의 최대 깊이가 " + maxLength + "mm로 너무 깊습니다. \n" +
                                    "축 방향 Groove의 최대 깊이는 폭의 3배인 " + (3 * depth) + "mm 이하로 모델링 해 주세요.")
                            .build()
            );
        }
        if (maxLength > 60) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("CNC_FRONT_BACK_GROOVE")
                            .message("축 방향 Groove의 최대 깊이가 " + maxLength + "mm로 너무 깊습니다. \n" +
                                    "축 방향 Groove의 최대 깊이는 60mm 이하로 모델링 해 주세요.")
                            .build()
            );
        }
    }
    
    // 선반 내경 보어 최대 길이 & 최대 직경
    private static void handleBoreLengthDiameter(JsonNode features, List<Estimate.ErrorDetail> errorDetailList) {
        JsonNode nvFeatures = features.path("nvFeatures");
        if (nvFeatures.isMissingNode()) return;
        JsonNode nvBores = nvFeatures.path("nvBores");
        if (nvBores.isEmpty() || nvBores.isMissingNode()) return;

        for (JsonNode bore : nvBores) {
            JsonNode basicPosition = bore.path("basicPosition");
            if (basicPosition == null || basicPosition.isNull() || basicPosition.isEmpty()) continue;

            boolean hasFront = false;
            boolean hasBack = false;
            for (JsonNode position : basicPosition) {
                if (position.asText().equals("front")) {
                    hasFront = true;
                } else if (position.asText().equals("back")) {
                    hasBack = true;
                }
            }

            double maxLength = (double) Math.round(bore.path("maxLength").asDouble() * 1000) / 1000;
            double maxDiameter = (double) Math.round(bore.path("maxDiameter").asDouble() * 1000) / 1000;

            if (maxDiameter < 1) {
                errorDetailList.add(
                        Estimate.ErrorDetail.builder()
                                .type("CNC_BORE")
                                .message("현재 중심 홀의 직경은 " + maxDiameter + "mm로 너무 작습니다. \n" +
                                        "최소 1mm 이상으로 모델링 해 주세요.")
                                .build()
                );
            }

            if (hasFront && hasBack) {
                if (maxLength > (maxDiameter * 8)) {
                    errorDetailList.add(
                            Estimate.ErrorDetail.builder()
                                    .type("CNC_BORE")
                                    .message("현재 중심 홀의 최대 깊이가 " + maxLength + "mm로 너무 깊습니다. \n" +
                                            "중심 홀 크기의 8배인 " + (maxDiameter * 8) + "mm 이하로 모델링 해 주세요.")
                                    .build()
                    );
                }
            } else if (hasFront || hasBack) {
                if (maxLength > (maxDiameter * 4)) {
                    errorDetailList.add(
                            Estimate.ErrorDetail.builder()
                                    .type("CNC_BORE")
                                    .message("현재 중심 홀의 최대 깊이가 " + maxLength + "mm로 너무 깊습니다. \n" +
                                            "중심 홀 크기의 4배인 " + (maxDiameter * 4) + "mm 이하로 모델링 해 주세요.")
                                    .build()
                    );
                }
            }

        }
    }

    // 내경 홈 관련 한계치
    private static void handleInternalGroove(JsonNode features, List<Estimate.ErrorDetail> errorDetailList) {
        JsonNode nvFeatures = features.path("nvFeatures");
        if (nvFeatures.isMissingNode()) return;
        JsonNode nvGrooves = nvFeatures.path("nvGrooves");
        if (nvGrooves.isEmpty() || nvGrooves.isMissingNode()) return;

        for (JsonNode groove : nvGrooves) {
            JsonNode basicPosition = groove.path("basicPosition");
            if (basicPosition == null || basicPosition.isNull() || basicPosition.isEmpty()) continue;

            boolean hasInner = false;

            for (JsonNode position : basicPosition) {
                if (position.asText().equals("inner")) {
                    hasInner = true;
                    break;
                }
            }

            if (!hasInner) continue;
            double maxDiameter = (double) Math.round(groove.path("maxDiameter").asDouble() * 1000) / 1000;
            double minDiameter = (double) Math.round(groove.path("minDiameter").asDouble() * 1000) / 1000;
            double depth = (maxDiameter - minDiameter) / 2;
            double maxLength = (double) Math.round(groove.path("maxLength").asDouble() * 1000) / 1000;

            if (depth > 10) {
                errorDetailList.add(
                        Estimate.ErrorDetail.builder()
                                .type("CNC_INTERNAL_GROOVE")
                                .message("현재 내경 홈 폭은 " + depth + "mm로 너무 깊습니다. \n" +
                                        "10mm 이하로 모델링 해 주세요.")
                                .build()
                );
            }
            if (maxLength < 1.15) {
                errorDetailList.add(
                        Estimate.ErrorDetail.builder()
                                .type("CNC_INTERNAL_GROOVE")
                                .message("현재 내경 홈 두께 " + maxLength + "mm로 너무 깊습니다. \n" +
                                        "1.15mm 이상으로 모델링 해 주세요.")
                                .build()
                );
            }
            if (minDiameter < 15) {
                errorDetailList.add(
                        Estimate.ErrorDetail.builder()
                                .type("CNC_INTERNAL_GROOVE")
                                .message("현재 내경 홈의 직경은 " + minDiameter + "mm로 너무 작습니다. \n" +
                                        "최소 15mm 이상으로 모델링 해 주세요.")
                                .build()
                );
            }
        }
    }

    // 심압대 센터홀 검사
    private static void handleCenterHole(JsonNode feature, List<Estimate.ErrorDetail> errorDetailList) {
        JsonNode maximumTurnedState = feature.path("maximumTurnedState");
        if (maximumTurnedState == null || maximumTurnedState.isNull() || maximumTurnedState.isMissingNode()) return;
        double maxDiameter = (double) Math.round(maximumTurnedState.path("maxDiameter").asDouble() * 1000) / 1000;
        double length = (double) Math.round(maximumTurnedState.path("length").asDouble() * 1000) / 1000;

        if ((length / maxDiameter) > 3) {
            errorDetailList.add(Estimate.ErrorDetail.builder()
                    .type("CNC_CENTER_HOLE_REQUIRED")
                    .message("길이 대비 직경 비율(L/D)이 3을 초과하는 선반물은 가공 중 불안정할 수 있으므로, 반대편 지지용 센터홀 가공이 필요합니다. \n" +
                            "센터홀 가공을 승인해주시기 바랍니다.")
                    .data(EstimateDTO.ErrorFlag.builder()
                            .flag(false)
                            .comment(CommentType.COMMENT_TYPE3)
                            .build())
                    .build());
        }
    }

    /**
     *
     * ///////////// 밀링이 아닌 경우에만 검사 끝 /////////////
     */

    /**
     *
     * //////////// 밀링인 경우에만 검사 /////////////
     */

    // 밀링 가공 최대 크기 검사
    private static Estimate.ErrorDetail handleMaxProcessingSizeMilling(JsonNode body) {
        JsonNode summary = body.path("summary");
        double bboxDx = summary.path("bboxDx").asDouble();
        double bboxDy = summary.path("bboxDy").asDouble();
        double bboxDz = summary.path("bboxDz").asDouble();

        // 최대 가공
        if (bboxDz > 350 || Math.max(bboxDx, bboxDy) > 1100 || Math.min(bboxDx, bboxDy) > 500) {
            return Estimate.ErrorDetail.builder()
                    .type("CNC_MAXIMUM_TURNED_STATE")
                    .message("밀링 최대 가공 사이즈를 벗어났습니다. \n500 x 1,100 x 350 이내로 모델링 해 주세요")
                    .build();
        }

        // 최소 가공
        if (bboxDz < 3 || Math.max(bboxDx, bboxDy) < 10 || Math.min(bboxDx, bboxDy) < 10) {
            return Estimate.ErrorDetail.builder()
                    .type("CNC_MINIMUM_TURNED_STATE")
                    .message("밀링 최소 가공 사이즈를 벗어났습니다. \n10 x 10 x 3 이상으로 모델링 해 주세요")
                    .build();
        }

        return null;
    }

    // 밀링 포켓 모서리 최소 R값
    private static Estimate.ErrorDetail handlePocketCorner(JsonNode features) {
        JsonNode prismaticMilling = features.path("prismaticMilling");
        if (prismaticMilling.isEmpty() || prismaticMilling.isMissingNode()) return null;

        for (JsonNode prismatic : prismaticMilling) {
            JsonNode configurations = prismatic.path("configurations");
            if (configurations.isEmpty() || configurations.isMissingNode()) continue;

            for (JsonNode config : configurations) {
                JsonNode walls = config.path("walls");
                if (walls.isEmpty() || walls.isMissingNode()) continue;

                for (JsonNode wall : walls) {
                    JsonNode maxConcaveCurvature = wall.path("maxConcaveCurvature");
                    if (maxConcaveCurvature == null || maxConcaveCurvature.isNull() || maxConcaveCurvature.asDouble() == 0) continue;

                    double minRadius = (double) Math.round( 1 / maxConcaveCurvature.asDouble() * 1000) / 1000;

                    if (minRadius < 1) {
                        return Estimate.ErrorDetail.builder()
                                .type("CNC_POCKET_CORNER")
                                .message("현재 포켓의 모서리 최소 R값이 " + minRadius + "mm로 너무 작습니다. \n" +
                                        "최소 1mm 이상으로 모델링 해 주세요.")
                                .build();
                    }
                }
            }
        }

        return null;
    }

    // 포켓 최대 가공 깊이 검사
    private static void handlePocketMaxDepth(JsonNode features, List<Estimate.ErrorDetail> errorDetailList) {
        // 샤프트 관련 faceId 수집
        Set<Integer> shaftFaceIds = new HashSet<>();
        JsonNode shaftsNode = features.path("shafts");
        for (JsonNode shaftNode : shaftsNode) {
            for (JsonNode faceIdNode : shaftNode.path("faceIds")) {
                shaftFaceIds.add(faceIdNode.asInt());
            }
        }

        // prismatic milling 분석
        JsonNode prismaticMillingNode = features.path("prismaticMilling");

        for (JsonNode prismaticItem : prismaticMillingNode) {
            for (JsonNode configNode : prismaticItem.path("configurations")) {
                double depth = configNode.path("depth").asDouble();

                JsonNode wallsNode = configNode.path("walls");
                List<Double> radiusList = new ArrayList<>();
                boolean hasShaftWall = false;
                int bottomFaceId = -1;

                for (JsonNode wallNode : wallsNode) {
                    int faceId = wallNode.path("faceId").asInt();
                    JsonNode curvatureNode = wallNode.path("maxConcaveCurvature");

                    if (!curvatureNode.isNull()) {
                        double curvature = curvatureNode.asDouble();
                        double radius = 1.0 / curvature;
                        radiusList.add(radius);
                    }

                    if (shaftFaceIds.contains(faceId)) {
                        hasShaftWall = true;

                        JsonNode bottomFacesNode = configNode.path("bottomFaces");
                        for (JsonNode bottomFaceNode : bottomFacesNode) {
                            for (JsonNode wallIdNode : bottomFaceNode.path("walls")) {
                                if (wallIdNode.asInt() == faceId) {
                                    bottomFaceId = bottomFaceNode.path("faceId").asInt();
                                    break;
                                }
                            }
                            if (bottomFaceId != -1) break;
                        }
                    }
                }

                boolean isThroughPocket = configNode.path("bottomFaces").isArray() &&
                        configNode.path("bottomFaces").isEmpty();

                // 샤프트 wall인 경우 바닥면에서 깊이 보정
                if (hasShaftWall && bottomFaceId != -1) {
                    System.out.println("특수 케이스: Wall이 샤프트의 일부, 바닥면 ID: " + bottomFaceId);
                    JsonNode milledFacesNode = features.path("milledFaces3Axes");

                    for (JsonNode milledFaceNode : milledFacesNode) {
                        if (milledFaceNode.path("faceId").asInt() == bottomFaceId) {
                            JsonNode pointDepthsNode = milledFaceNode.path("pointDepths");
                            if (pointDepthsNode.size() > 0) {
                                boolean allSame = true;
                                double firstDepth = pointDepthsNode.get(0).asDouble();
                                double maxDepth = firstDepth;

                                for (int i = 1; i < pointDepthsNode.size(); i++) {
                                    double currentDepth = pointDepthsNode.get(i).asDouble();
                                    maxDepth = Math.max(maxDepth, currentDepth);
                                    if (Math.abs(currentDepth - firstDepth) > 0.001) {
                                        allSame = false;
                                    }
                                }

                                if (allSame) {
                                    depth = firstDepth;
                                } else {
                                    depth = maxDepth;
                                }
                            }
                            break;
                        }
                    }
                }

                if (!radiusList.isEmpty()) {
                    double minDiameter = Collections.min(radiusList) * 2;

                    double allowedDepth = isThroughPocket ? 8 * minDiameter : 4 * minDiameter;

                    if (depth > allowedDepth) {
                        errorDetailList.add(
                                Estimate.ErrorDetail.builder()
                                        .type("CNC_POCKET_MAX_DEPTH")
                                        .message((isThroughPocket ? "관통" : "") + "포켓의 최대 가공 깊이는 포켓 모서리 최소 직경의 " +
                                                (isThroughPocket ? "8배" : "4배") + " 이내입니다. \n포켓의 모서리 R값을 키우거나 포켓 깊이를 낮춰주세요")
                                        .build()
                        );
                    }
                }

                int maxDepth = isThroughPocket ? 120 : 60;

                if (depth > maxDepth) {
                    errorDetailList.add(
                            Estimate.ErrorDetail.builder()
                                    .type("CNC_POCKET_MAX_DEPTH")
                                    .message("현재 포켓 깊이는 " + depth + "mm로 너무 깊습니다. \n" +
                                            "포켓 깊이를 "+ maxDepth +"mm 이하로 모델링 해 주세요.")
                                    .build()
                    );
                }
            }
        }
    }

    // 벽 최소 간격 검사
    private static void handleWallMinDistance(JsonNode features, List<Estimate.ErrorDetail> errorDetailList) {
        JsonNode prismaticMilling = features.path("prismaticMilling");
        if (prismaticMilling.isMissingNode() || prismaticMilling.isEmpty()) return;

        for (JsonNode prismatic : prismaticMilling) {
            JsonNode configurations = prismatic.path("configurations");

            if (configurations.isMissingNode() || configurations.isEmpty()) continue;

            for (JsonNode configuration : configurations) {
                JsonNode walls = configuration.path("walls");

                if (walls.isMissingNode() || walls.isEmpty()) continue;

                boolean allNull = true;

                for (JsonNode wall : walls) {
                    JsonNode maxConcaveCurvature = wall.path("maxConcaveCurvature");

                    if (!maxConcaveCurvature.isNull()) {
                        allNull = false;
                        break;
                    }
                }

                if (allNull) {
                    double depth = configuration.path("depth").asDouble();
                    double clearance = (double) Math.round(configuration.path("clearance").asDouble() * 1000) / 1000;

                    // 값이 존재하는지 확인 후 원하는 작업 수행
                    if (depth != 0 && clearance != 0) {
                        if (depth > 60) {
                            errorDetailList.add(
                                    Estimate.ErrorDetail.builder()
                                            .type("CNC_WALL_MIN_DISTANCE")
                                            .message("현재 가공 깊이는 " + depth + "mm로 너무 깊습니다. \n" +
                                                    "가공 깊이를 60mm 이하로 모델링 해 주세요.")
                                            .build()
                            );
                            return;
                        }
                        if (depth > (clearance * 4)) {
                            errorDetailList.add(
                                    Estimate.ErrorDetail.builder()
                                            .type("CNC_WALL_MIN_DISTANCE")
                                            .message("현재 가공 깊이는 " + depth + "mm로 너무 깊습니다. \n" +
                                                    "가공 깊이를 현재 벽 간격인 " + clearance + "mm 의 4배 이내인 "+ (clearance * 4) + "mm로 모델링 해 주세요.")
                                            .build()
                            );
                            return;
                        }
                    }
                }
            }
        }
    }
    

    /**
     *
     * //////////// 밀링인 경우에만 검사 끝 /////////////
     */

    // 리스트에 추가
    private static void addList(Estimate.ErrorDetail errorDetail, List<Estimate.ErrorDetail> errorDetailList) {
        if (errorDetail != null) {
            errorDetailList.add(errorDetail);
        }
    }


    /* ---------------------옵션 수정 시 검사 -----------------*/
    // 무게 찾기 (+1.5)
    public static Double extractKg(JsonNode body, String material) {
        JsonNode summary = body.path("summary");
        double volume = calcVolume(summary);
        log.info("부피 : {}", volume);
        return CNCMaterialDensity.calculateWeight(material, volume);
    }

    // 캠 작업 시간 , 세팅 시간
    public static CNCLimit.MachineTime calcMachineTime(JsonNode body, String cncType, CNCAxisAnalyzer.Result analysisResult) {
        JsonNode summary = body.path("summary");
        JsonNode features = body.path("features");
        JsonNode nvFeatures = features.path("nvFeatures");
        double removedVolume = calcRemoveVolume(summary); // 제거부피
        log.info("제거부피 : {}", removedVolume);
        double totalNvVolume = 0.0;
        int totalNVCount = 0;
        if (!nvFeatures.isMissingNode()) {
            log.info("nvFeatures 있음");
            String[] volumeFields = {"nvSteps", "nvBores", "nvGrooves"};
            for (String field : volumeFields) {
                JsonNode arrayNode = nvFeatures.path(field);
                if (arrayNode.isArray()) {
                    for (JsonNode node : arrayNode) {
                        totalNvVolume += node.path("volume").asDouble();
                    }
                }
            }
            int stepCount = nvFeatures.path("nvSteps").size();
            int boreCount = nvFeatures.path("nvBores").size();
            int grooveCount = nvFeatures.path("nvGrooves").size();
            totalNVCount = (stepCount * 60 * 5) + (boreCount * 60 * 4) + (grooveCount * 60 * 4);
        }

        double camTime = 0.0; // 캠 작업 시간
        double camLatheCalc = totalNVCount + (totalNvVolume / 100000); // 선반 캠 작업 시간
        double camMillingCalc = (
                (0.00005 * removedVolume) +
                ( ((double) analysisResult.getTotalProcessingCount() / 2) + ((double) analysisResult.getTotal3DAreaCount() /2) ) +
                (analysisResult.getStandardAxisCount() * 10) )
                * 60; // 밀링 캠 작업 시간

        double settingTime = 0.0; // 셋팅 시간
        double settingLatheCalc = totalNVCount + (totalNvVolume / 100000); // 선반 세팅 시간
        double settingMillingCalc =( (0.00005 * removedVolume) +
                ( ((double) analysisResult.getTotalProcessingCount() /2)
                        + ((double) analysisResult.getTotal3DAreaCount() /2) )+
                (analysisResult.getStandardAxisCount() * 10) ) * 60; // 밀링 세팅 시간;

        switch (cncType) {
            case "CNC_PURE_LATHE": case "CNC_LATHE" :
                camTime = camLatheCalc;
                settingTime = settingLatheCalc;
                break;
            case "CNC_MILLING" :
                camTime = camMillingCalc;
                settingTime = settingMillingCalc;
                break;
            case "CNC_LATHE_MILLING" :
                camTime = camLatheCalc + camMillingCalc;
                settingTime = settingLatheCalc + settingMillingCalc;
                break;
            default:
                throw new IllegalArgumentException("잘못된 CNC 타입입니다. : " + cncType);
        }

        camTime = camTime < 600 ? 600 : camTime;
        settingTime = settingTime < 600 ? 600 : settingTime;

        CNCLimit.MachineTime obj = CNCLimit.MachineTime.builder()
                .camTime(camTime)
                .settingTime(settingTime)
                .build();

        return obj;
    }

    // chamfer 작업 시간
    public static double calcChamferTime(JsonNode body) {
        JsonNode summary = body.path("summary");
        double totalEdgeLength = summary.path("totalEdgeLength").asDouble();
        double chamferTime = totalEdgeLength / 60; // 1초당 10mm
        return chamferTime;
    }

    // 가공시간 (소재별 비율)
    public static double calcProcessingTime(JsonNode body, CostDTO.CnCCostDTO costDTO, CNCAxisAnalyzer.Result analysisResult) {
        JsonNode summary = body.path("summary");
        double removeVolume = calcRemoveVolume(summary);
        double processingTime = 0.0;
        double latheCalc = (removeVolume * 0.85 / 5250) + (removeVolume * 0.15 / 120);
        double millingCalc = (removeVolume * 0.8 / 3600) + (removeVolume * 0.08 / 140) + (removeVolume * 0.04/18); // 기본시간
        for (double minDiameter : analysisResult.getMinDiameterByAxis()) { // 최적 가공축 개수만큼 더함 (최적 가공축과 최소 직경은 1대1매핑)
            millingCalc += (removeVolume * 0.08 / analysisResult.getStandardAxisCount() / (minDiameter * 4.5));
        }

        Double machineRate = CNCMaterialMachiningRate.getMachiningRate(costDTO.getMaterial()); // 가공속도비율
        if (machineRate == null) {
            throw new RuntimeException("재질정보가 없습니다. 선택하신 재질 : " + costDTO.getMaterial());
        }

        switch (costDTO.getCncType()) {
            case "CNC_PURE_LATHE": case "CNC_LATHE":
                processingTime = latheCalc;
                break;
            case "CNC_MILLING":
                processingTime = millingCalc;
                break;
            case "CNC_LATHE_MILLING":
                processingTime = latheCalc + millingCalc;
                break;
            default:
                throw new IllegalArgumentException("잘못된 CNC 가공 타입입니다 : " + costDTO.getCncType());
        }

        return processingTime * machineRate; // 가공속도비율 곱하기
    }

    // 3D 가공시간
    public static double calc3DArea(CNCAxisAnalyzer.Result analysisResult) {

        return analysisResult.getTotal3DArea() / 2;
    }

    // 탭 비용
    public static double calcTapCost(String holeJson) {
        if (holeJson == null || holeJson.isEmpty()) return 0.0;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode holeNodes = null;
        try{
            holeNodes = objectMapper.readTree(holeJson);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }

        int tapCost = CNCCostByHole.getCostByHole("TAP"); // 개당 단가
        double totalCost = 0.0;

        for (JsonNode holeObj : holeNodes) {
            String type = holeObj.path("type").asText(); // 구멍 타입
            int cnt = holeObj.path("count").asInt(); // 구멍 개수

            if (type.startsWith("M")) {
                double size = Double.parseDouble(type.replace("M", ""));
                totalCost += size * tapCost * cnt;
            }
        }

        return totalCost;
    }

    // 지그비용
    public static double calcJigCost(CNCPrice.CalcPrice calcPrice, CNCAxisAnalyzer.Result result) {
        if (result.getTotalJigCount() == 0) return 0.0;
        double jigCost = 0.0;
        int MIN_COST = 15000; // 지그 1개 최소 비용
        int MAX_COST = 60000; // 지그 1개 최대 비용
        double calcCost = (calcPrice.getMaterialCost() + ((calcPrice.getChamferTime() + calcPrice.getProcessingTime()) * 10))
                * 0.1; // 지그 1개당 계산가격

        if (calcCost < MIN_COST) {
            jigCost = MIN_COST * result.getTotalJigCount();
        } else if (calcCost > MAX_COST) {
            jigCost = MAX_COST * result.getTotalJigCount();
        } else {
            jigCost = calcCost * result.getTotalJigCount();
        }

        return jigCost;
    }

    // 표면처리 비용 (후가공)
    public static double calcSurfaceCost(CostDTO.CnCCostDTO costDTO) {
        JsonNode summary = costDTO.getBodyJson().path("summary");
        double totalVolume = summary.path("totalVolume").asDouble();
        Double kg = CNCMaterialDensity.calculateWeight(costDTO.getMaterial(), totalVolume);
        if (kg == null) {
            throw new RuntimeException("재질정보가 없습니다. 선택하신 재질 : " + costDTO.getMaterial());
        }
        Double surfaceCost = CNCCostBySurface.getCostBySurface(costDTO.getSurface(), kg);

        return surfaceCost;
    }

    // 공차비용
    public static double calcCommonDiffCost(CostDTO.CnCCostDTO costDTO, CNCPrice.CalcPrice calcPrice) {
        double commonDiffCost = 0.0;
        double calcCost = ( calcPrice.getMaterialCost() +
                ( calcPrice.getChamferTime() + calcPrice.getProcessingTime() )
                    * 10 );
        log.info("계산 가격: {}", calcCost);
        log.info("공차 옵션 : {} ", costDTO.getCommonDiff());
        switch (costDTO.getCommonDiff()) {
            case "D0":
                commonDiffCost = 0.0;
                break;
            case "D1": // +-0.10mm
                commonDiffCost = calcCost * 0.05;
                break;
            case "D2": // +-0.05mm
                commonDiffCost = calcCost * 0.15;
                break;
            default:
                throw new IllegalArgumentException("잘못된 공차 옵션입니다. \n공차옵션 : " + costDTO.getCommonDiff());
        }
        return commonDiffCost;
    }

    // 표면거칠기 비용
    public static double calcRoughnessCost(CostDTO.CnCCostDTO costDTO, CNCPrice.CalcPrice calcPrice) {
        double roughnessCost = 0.0;
        double calcCost = ( calcPrice.getMaterialCost() +
                ( calcPrice.getChamferTime() + calcPrice.getProcessingTime() )
                * 10 );
        switch (costDTO.getRoughness()) {
            case "R0": // Ra3.2
                roughnessCost = 0.0;
                break;
            case "R1": // Ra1.6
                roughnessCost = calcCost * 0.05;
                break;
            default:
                throw new IllegalArgumentException("잘못된 표면거칠기 옵션입니다. \n표면거칠기 옵션 : " + costDTO.getRoughness());
        }
        return roughnessCost;
    }

    // 부피찾기 (+1.5)
    private static double calcVolume(JsonNode summary) {
        double boxDx = summary.path("bboxDx").asDouble();
        double boxDy = summary.path("bboxDy").asDouble();
        double boxDz = summary.path("bboxDz").asDouble();
        return (boxDx + 1.5) * (boxDy + 1.5) * (boxDz + 1.5);
    }

    // 제거부피 찾기
    private static double calcRemoveVolume(JsonNode summary) {
        double volume = calcVolume(summary); // +1.5 부피
        double totalVolume = summary.path("totalVolume").asDouble(); // 전체 부피
        double removedVolume = volume - totalVolume; // 제거부피

        return removedVolume;
    }

    // totalVolume 무게 찾기 (totalVolume)
    public static double calcTotalVolumeKg(JsonNode body, String material) {
        JsonNode summary = body.path("summary");
        double totalVolume = summary.path("totalVolume").asDouble(); // 전체 부피
        Double kg = CNCMaterialDensity.calculateWeight(material, totalVolume);
        if (kg == null) {
            throw new RuntimeException("재질정보가 없습니다. 선택하신 재질 : " + material);
        }
        log.info("totalVolume 무게 : {}", kg);
        return kg;
    }

    // 리프팅 홀 찾기
    public static String findLiftHole(JsonNode parts, String material) {
        JsonNode body = parts.get(0).path("bodies").get(0);
        JsonNode summary = body.path("summary");
        double volume = summary.path("totalVolume").asDouble();
        Double kg = CNCMaterialDensity.calculateWeight(material, volume);
        if(kg == null){
            return "올바른 재질을 선택해주세요";
        }
        double boxDx = summary.path("bboxDx").asDouble();
        double boxDy = summary.path("bboxDy").asDouble();
        double boxDz = summary.path("bboxDz").asDouble();
        double maxBox = Math.max(boxDx, Math.max(boxDy, boxDz));

        JsonNode features = body.path("features");
        JsonNode holes = features.path("holes");
        JsonNode prismaticMilling = features.path("prismaticMilling");
        if (holes.isMissingNode() && prismaticMilling.isMissingNode()) return null;
        int holeCnt = 0;
        if (!holes.isMissingNode()) {
            for (JsonNode hole: holes) {
                JsonNode bottom = hole.path("bottom");
                if (!bottom.isNull()) continue;
                JsonNode bore = hole.path("bores").get(0);
                double diameter = bore.path("diameter").asDouble();
                log.info("리프팅 홀 직경 : {}", diameter);
                if (diameter >= 4) {
                    holeCnt++;
                }
            }
        }

        if (!prismaticMilling.isMissingNode()) {
            for (JsonNode milling : prismaticMilling) {
                JsonNode configuration = milling.path("configurations").get(0);
                double clearance = configuration.path("clearance").asDouble();
                JsonNode bottom = configuration.path("bottomFaces");
                if (!bottom.isEmpty()) continue;
                log.info("리프팅 형상 크기: {}", clearance);
                if (clearance >= 4) {
                    holeCnt++;
                }
            }
        }
        log.info("리프팅 홀 개수 : {}", holeCnt);
        if(kg >= 8 && holeCnt < 2){
            return "모델의 중량이 8kg 이상이므로 관통홀을 2곳 이상 모델링해주세요.";
        }
        if(maxBox >= 420 && holeCnt < 2){
            return "모델의 가장 긴쪽이 420mm 이상이므로 관통홀을 2곳 이상 모델링해주세요.";
        }
        if(holeCnt < 1){
            return "표면처리를 위해 리프팅 홀이 필요합니다. \n4pi 이상홀이나 최소 간격이 4이상인 형상으로 모델링 해 주세요.";
        }

        return null;
    }

    // 옵션수정 시 한계치 검사
    public static String findLimitError(JsonNode parts, String type, EstimateDTO.CnCOptionPut optionPut, List<Estimate.ErrorDetail> errorDetailList) {
        JsonNode body = parts.get(0).get("bodies").get(0);
        JsonNode features = body.path("features");
        if (!FileCommon.isSameMethod(type, MethodType.CNC_MILLING)) { // CNC 밀링이 아닐 경우
            

        } else { // CNC 밀링일 경우
            // 홀 최소 직경 검사
            handleHoleMinDiameter(features, errorDetailList, optionPut.getMaterial());
        }


        return FileCommon.convertListToString(errorDetailList);
    }

    // 홀 최소 직경 검사,  홀 드릴가공 or 엔드밀 가공
    private static void handleHoleMinDiameter(JsonNode features, List<Estimate.ErrorDetail> errorDetailList, String material) {

        String majorMaterial = switch (material) {
            case "SM45C", "SKD11", "SS400" -> "STEEL";
            case "SUS303", "SUS304", "SUS316" -> "STAIN";
            case "AL6061", "AL7075", "AL5052", "BRASS_C3604" -> "NON_STEEL";
            case "POM_WHITE", "POM_BLACK", "MC_NYLON_BLUE", "MC_NYLON_IVORY", "PEEK" -> "RESIN";
            default -> throw new RuntimeException("잘못된 재질입니다. : " + material);
        };

        JsonNode holes = features.path("holes");
        if (holes.isMissingNode() || holes.isEmpty()) return;
        for (JsonNode hole : holes) {
            JsonNode bores = hole.path("bores");
            if (bores.isMissingNode() || bores.isEmpty()) continue;
            for (JsonNode bore : bores) {
                double diameter = bore.path("diameter").asDouble();
                if (CNCDrillHoleSize.isDrilled(diameter)) { // 드릴로 가공 가능한 직경인지 검사.
                    JsonNode bottom = hole.path("bottom");
                    if (bottom.isMissingNode() || bottom.isNull() || bottom.isEmpty()) continue;
                    double bottomAngle = (double) Math.round(bottom.path("bottomAngle").asDouble() * 100) / 100;
                    if (bottomAngle == 118) {
                        log.info("118도 드릴 홀임");
                        handleProcessingByDrill(hole, majorMaterial, errorDetailList);
                    }else if (bottomAngle == 0 || bottomAngle == 180) {
                        log.info("바텀 앵글 0, 180도 엔드밀 홀임");
                        handleProcessingByEndMill(hole, majorMaterial, errorDetailList);
                    } else {
                        errorDetailList.add(
                                Estimate.ErrorDetail.builder()
                                        .type("CNC_HOLE_BOTTOM_ANGLE")
                                        .message("홀의 바닥면 각도는 " + bottomAngle + "도입니다. \n홀의 바닥면 각도는 0도 혹은 118도만 가능합니다.")
                                        .build()
                        );
                        return;
                    }
                } else { // 엔드밀
                    log.info("드릴 직경 안돼는 엔드밀 홀임");
                    handleProcessingByEndMill(hole, majorMaterial, errorDetailList);
                }
            }
        }
    }

    // 드릴 가공 홀일때 가공 깊이 & 직경 검사
    private static void handleProcessingByDrill(JsonNode hole, String material, List<Estimate.ErrorDetail> errorDetailList) {
        JsonNode countersink = hole.path("countersinks").get(0);
        JsonNode counterbore = hole.path("counterbores").get(0);
        JsonNode bore = hole.path("bores").get(0);
        if (bore == null || bore.isNull()) return;

        if (countersink != null && !countersink.isNull()) { // 카운터싱크 일경우
            log.info("드릴 : 카운터 싱크 입니다");
            double counterSinkDepth = countersink.path("depth").asDouble();
            double boreDepth = bore.path("depth").asDouble();
            double depth = counterSinkDepth + boreDepth;
            double diameter = bore.path("diameter").asDouble();
            validateHoleMachinabilityByDrill(depth, diameter, errorDetailList); // 홀 가공 가능 여부 검사
        } else if (counterbore != null && !counterbore.isNull()) { // 카운터 보어일 경우
            log.info("드릴 : 카운터 보어 입니다");
            double counterBoreDepth = counterbore.path("depth").asDouble();
            double counterBoreDiameter = counterbore.path("diameter").asDouble();
            double boreDepth = bore.path("depth").asDouble();
            double depth = counterBoreDepth + boreDepth;
            double diameter = bore.path("diameter").asDouble();
            validateHoleMachinabilityByDrill(depth, diameter, errorDetailList); // 홀 가공 가능 여부 검사
            validateHoleMachinalilityByEndMill(counterBoreDepth, counterBoreDiameter, material, errorDetailList); // 홀 가공 가능 여부 검사
        } else { // 일반 홀일 경우
            log.info("드릴 : 일반 홀 입니다");
            double diameter = bore.path("diameter").asDouble();
            double depth = bore.path("depth").asDouble();

            validateHoleMachinabilityByDrill(depth, diameter, errorDetailList); // 홀 가공 가능 여부 검사
        }
    }

    // 엔드밀 가공 홀일때 가공 깊이 & 직경 검사
    private static void handleProcessingByEndMill(JsonNode hole, String material, List<Estimate.ErrorDetail> errorDetailList) {
        JsonNode countersink = hole.path("countersinks").get(0);
        JsonNode counterbore = hole.path("counterbores").get(0);
        JsonNode bore = hole.path("bores").get(0);
        if (bore == null || bore.isNull()) return;

        if (countersink != null && !countersink.isNull()) { // 카운터싱크 일경우
            log.info("엔드밀 : 카운터 싱크 입니다");
            double counterSinkDepth = countersink.path("depth").asDouble();
            double boreDepth = bore.path("depth").asDouble();
            double depth = counterSinkDepth + boreDepth;
            double diameter = bore.path("diameter").asDouble();

            validateHoleMachinalilityByEndMill(depth, diameter, material, errorDetailList); // 홀 가공 가능 여부 검사
        } else if (counterbore != null && !counterbore.isNull()) { // 카운터 보어일 경우
            log.info("엔드밀 : 카운터 보어 입니다");
            double counterBoreDepth = counterbore.path("depth").asDouble();
            double counterBoreDiameter = counterbore.path("diameter").asDouble();
            double boreDepth = bore.path("depth").asDouble();
            double depth = counterBoreDepth + boreDepth;
            double diameter = bore.path("diameter").asDouble();
            
            validateHoleMachinalilityByEndMill(depth, diameter, material, errorDetailList); // 홀 가공 가능 여부 검사
            validateHoleMachinalilityByEndMill(counterBoreDepth, counterBoreDiameter, material, errorDetailList); // 홀 가공 가능 여부 검사
        } else { // 일반 홀일 경우
            log.info("엔드밀 : 일반 홀 입니다");
            double diameter = bore.path("diameter").asDouble();
            double depth = bore.path("depth").asDouble();

            validateHoleMachinalilityByEndMill(depth, diameter, material, errorDetailList); // 홀 가공 가능 여부 검사
        }
    }

    // 홀 깊이와 직경 검증 (드릴)
    private static void validateHoleMachinabilityByDrill(double depth, double diameter, List<Estimate.ErrorDetail> errorDetailList) {
        if ( diameter < 2 || diameter > 30) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("CNC_HOLE_DIAMETER")
                            .message("현재 홀의 직경은 " + (Math.round(diameter * 1000) / 1000) + "mm로 가공 한계를 초과합니다. \n" +
                                    "홀의 직경을 2mm 이상 30mm 이하로 모델링 해 주세요.")
                            .build()
            );
            return;
        }

        if (depth > 280) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("CNC_HOLE_DEPTH")
                            .message("현재 홀의 깊이는 " + (Math.round(depth * 1000) / 1000) + "mm로 가공 한계를 초과합니다. \n" +
                                    "홀의 깊이를 280mm 이하로 모델링 해 주세요.")
                            .build()
            );
            return;
        }

        if ((2 <= diameter && diameter <= 10) && depth > (diameter * 20) ) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("CNC_HOLE_DIAMETER")
                            .message("현재 홀의 깊이가 " + (Math.round(depth * 1000) / 1000) + "mm로 너무 깊습니다. \n" +
                                    "홀의 깊이를 홀 직경의 20배 이내인 "+ (diameter * 20) + "mm로 모델링 해 주세요.")
                            .build()
            );
        }
    }

    // 홀 깊이와 직경 검증 (엔드밀)
    private static void validateHoleMachinalilityByEndMill(double depth, double diameter, String material, List<Estimate.ErrorDetail> errorDetailList) {
        if (diameter < 1.499) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("CNC_HOLE_DIAMETER")
                            .message("현재 홀의 직경은 " + (Math.round(diameter * 1000) / 1000) + "mm로 가공 한계를 초과합니다. \n" +
                                    "홀의 직경을 1.5mm 이상으로 모델링 해 주세요.")
                            .build()
            );
            return;
        }

        if (CNCEndMillSize.getDepthLimitByDiameter(material, diameter) < depth) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("CNC_HOLE_DEPTH")
                            .message("현재 홀의 깊이는 " + (Math.round(depth * 1000) / 1000) + "mm로 가공 한계를 초과합니다. \n" +
                                    "홀의 깊이를 "+ CNCEndMillSize.getDepthLimitByDiameter(material, diameter) +"mm 이하로 모델링 해 주세요.")
                            .build()
            );
            return;
        }
        if (!CNCEndMillSize.isValidDepthByDiameter(material, diameter, depth)) {
            errorDetailList.add(
                    Estimate.ErrorDetail.builder()
                            .type("CNC_HOLE_DEPTH")
                            .message("현재 홀의 깊이는 " + (Math.round(depth * 1000) / 1000) + "mm로 가공 한계를 초과합니다. \n" +
                                    "홀의 깊이를 "+ CNCEndMillSize.getDepthLimitByDiameter(material, diameter) +"mm 이하로 모델링 해 주세요.")
                            .build()
            );
        }
    }

    // 옵션 수정 시 홀 / 탭 검사
    public static boolean checkTapHole(String holeJson) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode holeList = null;
        try {
            holeList = mapper.readTree(holeJson);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("잘못된 JSON 형식입니다.");
        }
        if (holeList == null) return false;

        for (JsonNode hole : holeList) {
            String type = hole.path("type").asText();
            if (type.equals("hole")) continue;

            double diameter = hole.path("diameter").asDouble();
            boolean isValid = CNCHoleSize.isValidHoleSize(type, diameter);
            if (!isValid) throw new RuntimeException("허용 하지 않는 탭 사이즈입니다.");
        }

        return true;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MachineTime {
        private double camTime; // 캠 작업 시간
        private double settingTime; // 셋팅 시간
    }
}
