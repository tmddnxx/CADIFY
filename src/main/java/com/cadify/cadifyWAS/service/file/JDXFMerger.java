//package com.cadify.cadifyWAS.service.file;
//
//import com.jsevy.jdxf.*;
//import java.awt.*;
//import java.awt.geom.AffineTransform;
//import java.io.*;
//import java.util.List;
//import java.util.Arrays;
//
///**
// * JDXF 라이브러리를 사용하여 여러 DXF 파일을 하나로 합치는 클래스
// */
//public class JDXFMerger {
//
//    private DXFDocument mergedDocument;
//    private DXFGraphics graphics;
//    private double currentXOffset = 0;
//    private double currentYOffset = 0;
//    private double offsetIncrement = 1000;
//    private int layerCounter = 1;
//
//    /**
//     * 생성자 - 새로운 DXF 문서 초기화
//     */
//    public JDXFMerger(String documentName) {
//        this.mergedDocument = new DXFDocument(documentName);
//        this.graphics = mergedDocument.getGraphics();
//    }
//
//    /**
//     * 오프셋 증가값 설정 (파일 간 간격)
//     */
//    public void setOffsetIncrement(double increment) {
//        this.offsetIncrement = increment;
//    }
//
//    /**
//     * 여러 DXF 파일을 병합하는 메인 메서드
//     */
//    public void mergeFiles(List<String> inputFiles, String outputFile) throws IOException {
//        System.out.println("DXF 파일 병합 시작...");
//
//        for (int i = 0; i < inputFiles.size(); i++) {
//            String filePath = inputFiles.get(i);
//            System.out.println("처리 중: " + filePath);
//
//            // 각 파일을 다른 레이어에 배치
//            String layerName = "Layer_" + layerCounter;
//            mergedDocument.setLayer(layerName);
//
//            // 변환 매트릭스 설정 (오프셋 적용)
//            AffineTransform transform = new AffineTransform();
//            transform.translate(currentXOffset, currentYOffset);
//            graphics.setTransform(transform);
//
//            // 파일 내용 처리
//            if (new File(filePath).exists()) {
//                // 실제 DXF 파일이 있는 경우 파싱해서 그리기
//                parseDXFFile(filePath, layerName);
//            } else {
//                // 파일이 없는 경우 예시 콘텐츠 그리기
//                drawExampleContent(i + 1, layerName);
//            }
//
//            // 다음 파일을 위한 오프셋 업데이트
////            updateOffset();
//            layerCounter++;
//        }
//
//        // 병합된 결과 저장
//        saveToFile(outputFile);
//        System.out.println("병합 완료: " + outputFile);
//    }
//
//    /**
//     * 단일 DXF 파일 추가
//     */
//    public void addDXFFile(String filePath, String layerName) throws IOException {
//        if (layerName == null || layerName.trim().isEmpty()) {
//            layerName = "Layer_" + layerCounter++;
//        }
//
//        mergedDocument.setLayer(layerName);
//
//        // 현재 오프셋으로 변환 설정
//        AffineTransform transform = new AffineTransform();
//        transform.translate(currentXOffset, currentYOffset);
//        graphics.setTransform(transform);
//
//        if (checkFileExists(filePath)) {
//            String normalizedPath = validateAndNormalizePath(filePath);
//            parseDXFFile(normalizedPath, layerName);
//        } else {
//            drawExampleContent(layerCounter, layerName);  // layerCounter 사용
//        }
//
////        updateOffset();
//    }
//
//    /**
//     * 사용자 정의 그리기 작업 추가
//     */
//    public void addCustomDrawing(String layerName, DrawingTask task) {
//        if (layerName == null || layerName.trim().isEmpty()) {
//            layerName = "Custom_" + layerCounter++;
//        }
//
//        mergedDocument.setLayer(layerName);
//
//        // 현재 오프셋으로 변환 설정
//        AffineTransform transform = new AffineTransform();
//        transform.translate(currentXOffset, currentYOffset);
//        graphics.setTransform(transform);
//
//        // 사용자 정의 그리기 실행
//        task.draw(graphics);
//
////        updateOffset();
//    }
//
//    /**
//     * DXF 파일 파싱 및 그리기 (실제 구현)
//     */
//    private void parseDXFFile(String filePath, String layerName) throws IOException {
//        System.out.println("  파일 파싱: " + filePath + " -> 레이어: " + layerName);
//
//        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            boolean inEntitiesSection = false;
//            boolean entityDrawn = false;
//
//            // DXF 파싱을 위한 변수들
//            String currentEntity = null;
//            double x1 = 0, y1 = 0, x2 = 0, y2 = 0;
//            double centerX = 0, centerY = 0, radius = 0;
//            int groupCode = -1;
//
//            while ((line = reader.readLine()) != null) {
//                line = line.trim();
//
//                // 그룹 코드인지 확인
//                try {
//                    groupCode = Integer.parseInt(line);
//                    continue;
//                } catch (NumberFormatException e) {
//                    // 그룹 코드가 아님, 값으로 처리
//                }
//
//                // ENTITIES 섹션 확인
//                if (line.equals("ENTITIES")) {
//                    inEntitiesSection = true;
//                    continue;
//                }
//
//                if (line.equals("ENDSEC") && inEntitiesSection) {
//                    break;
//                }
//
//                if (!inEntitiesSection) continue;
//
//                // 엔티티 타입 확인
//                if (groupCode == 0) { // 엔티티 타입
//                    // 이전 엔티티 그리기
//                    if (currentEntity != null) {
//                        entityDrawn = drawParsedEntity(currentEntity, x1, y1, x2, y2, centerX, centerY, radius) || entityDrawn;
//                    }
//
//                    currentEntity = line;
//                    // 좌표 초기화
//                    x1 = y1 = x2 = y2 = centerX = centerY = radius = 0;
//                }
//                // 좌표 데이터 파싱
//                else if (groupCode == 10) { // X1 좌표
//                    x1 = Double.parseDouble(line);
//                } else if (groupCode == 20) { // Y1 좌표
//                    y1 = Double.parseDouble(line);
//                } else if (groupCode == 11) { // X2 좌표
//                    x2 = Double.parseDouble(line);
//                } else if (groupCode == 21) { // Y2 좌표
//                    y2 = Double.parseDouble(line);
//                } else if (groupCode == 40) { // 반지름
//                    radius = Double.parseDouble(line);
//                }
//            }
//
//            // 마지막 엔티티 그리기
//            if (currentEntity != null) {
//                entityDrawn = drawParsedEntity(currentEntity, x1, y1, x2, y2, centerX, centerY, radius) || entityDrawn;
//            }
//
//            if (!entityDrawn) {
//                System.out.println("  DXF에서 그릴 수 있는 엔티티를 찾지 못했습니다. 예시 콘텐츠를 그립니다.");
//                drawExampleContent(layerCounter, layerName);
//            } else {
//                System.out.println("  DXF 파싱 및 그리기 완료: " + layerName);
//            }
//
//        } catch (IOException e) {
//            System.err.println("파일 읽기 오류: " + filePath);
//            throw e;
//        } catch (NumberFormatException e) {
//            System.err.println("DXF 파싱 오류: " + filePath + " - " + e.getMessage());
//            drawExampleContent(layerCounter, layerName);
//        }
//    }
//
//    /**
//     * 파싱된 엔티티를 실제로 그리기
//     */
//    private boolean drawParsedEntity(String entityType, double x1, double y1, double x2, double y2,
//                                     double centerX, double centerY, double radius) {
//        try {
//            graphics.setColor(Color.BLACK); // 기본 색상
//            graphics.setStroke(new BasicStroke(1)); // 기본 선 두께
//
//            switch (entityType.toUpperCase()) {
//                case "LINE":
//                    if (x1 != 0 || y1 != 0 || x2 != 0 || y2 != 0) {
//                        graphics.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
//                        return true;
//                    }
//                    break;
//
//                case "CIRCLE":
//                    if (radius > 0) {
//                        int diameter = (int)(radius * 2);
//                        graphics.drawOval((int)(centerX - radius), (int)(centerY - radius), diameter, diameter);
//                        return true;
//                    }
//                    break;
//
//                case "ARC":
//                    if (radius > 0) {
//                        int diameter = (int)(radius * 2);
//                        graphics.drawArc((int)(centerX - radius), (int)(centerY - radius),
//                                diameter, diameter, 0, 180); // 기본 반원
//                        return true;
//                    }
//                    break;
//
//                case "LWPOLYLINE":
//                case "POLYLINE":
//                    // 폴리라인은 복잡하므로 기본 사각형으로 대체
//                    if (x1 != x2 && y1 != y2) {
//                        graphics.drawRect((int)Math.min(x1, x2), (int)Math.min(y1, y2),
//                                (int)Math.abs(x2 - x1), (int)Math.abs(y2 - y1));
//                        return true;
//                    }
//                    break;
//            }
//        } catch (Exception e) {
//            System.err.println("엔티티 그리기 오류: " + entityType + " - " + e.getMessage());
//        }
//
//        return false;
//    }
//
//    /**
//     * 예시 콘텐츠 그리기
//     */
//    private void drawExampleContent(int fileNumber, String layerName) {
//        // 색상 설정
//        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE,
//                Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW};
//
//        // fileNumber가 0 이하인 경우를 방지
//        int colorIndex = Math.max(0, fileNumber - 1) % colors.length;
//        graphics.setColor(colors[colorIndex]);
//
//        // 선 두께 설정
//        graphics.setStroke(new BasicStroke(2));
//
//        // 기본 도형들 그리기
//        graphics.drawRect(0, 0, 200, 100);
//        graphics.drawOval(50, 50, 100, 100);
//        graphics.drawLine(0, 0, 200, 100);
//        graphics.drawLine(200, 0, 0, 100);
//
//        // 원 그리기
//        graphics.drawOval(250, 50, 80, 80);
//
//        // 텍스트 추가
//        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
//        graphics.drawString("Layer: " + layerName, 10, 200);
//        graphics.drawString("File #" + fileNumber, 10, 220);
//
//        System.out.println("  예시 콘텐츠 그리기 완료: " + layerName);
//    }
//
//    /**
//     * 파일 경로 검증 및 정규화
//     */
//    private String validateAndNormalizePath(String filePath) {
//        // 절대 경로인 경우
//        if (filePath.startsWith("/") || filePath.matches("^[A-Za-z]:.*")) {
//            return filePath;
//        }
//
//        // 상대 경로인 경우 현재 작업 디렉토리 기준으로 변환
//        String currentDir = System.getProperty("user.dir");
//        return currentDir + File.separator + filePath.replace("/", File.separator);
//    }
//
//    /**
//     * 파일 존재 여부 상세 체크
//     */
//    private boolean checkFileExists(String filePath) {
//        String normalizedPath = validateAndNormalizePath(filePath);
//        File file = new File(normalizedPath);
//
//        System.out.println("  파일 경로 체크:");
//        System.out.println("    원본 경로: " + filePath);
//        System.out.println("    정규화된 경로: " + normalizedPath);
//        System.out.println("    파일 존재: " + file.exists());
//        System.out.println("    절대 경로: " + file.getAbsolutePath());
//        System.out.println("    읽기 가능: " + file.canRead());
//
//        if (!file.exists()) {
//            System.err.println("    파일을 찾을 수 없습니다: " + normalizedPath);
//            // 현재 디렉토리의 파일 목록 출력
//            File currentDir = new File(System.getProperty("user.dir"));
//            System.out.println("  현재 디렉토리 파일 목록:");
//            File[] files = currentDir.listFiles();
//            if (files != null) {
//                for (File f : files) {
//                    if (f.getName().endsWith(".dxf")) {
//                        System.out.println("    - " + f.getName());
//                    }
//                }
//            }
//            return false;
//        }
//
//        return true;
//    }
//
//    /**
//     * 병합된 DXF를 파일로 저장
//     */
//    public void saveToFile(String outputPath) throws IOException {
//        String dxfContent = mergedDocument.toDXFString();
//
//        try (FileWriter writer = new FileWriter(outputPath)) {
//            writer.write(dxfContent);
//            writer.flush();
//        }
//
//        System.out.println("파일 저장 완료: " + outputPath);
//    }
//
//    /**
//     * 병합된 DXF 내용을 문자열로 반환
//     */
//    public String getDXFContent() {
//        return mergedDocument.toDXFString();
//    }
//
//    /**
//     * 현재 문서 상태 리셋
//     */
//    public void reset(String newDocumentName) {
//        this.mergedDocument = new DXFDocument(newDocumentName);
//        this.graphics = mergedDocument.getGraphics();
//        this.currentXOffset = 0;
//        this.currentYOffset = 0;
//        this.layerCounter = 1;
//    }
//
//    /**
//     * 사용자 정의 그리기 작업을 위한 함수형 인터페이스
//     */
//    @FunctionalInterface
//    public interface DrawingTask {
//        void draw(DXFGraphics graphics);
//    }
//
//    /**
//     * 사용 예시 메인 메서드
//     */
//    public static void dxfTest() {
//        try {
//            // JDXFMerger 인스턴스 생성
//            JDXFMerger merger = new JDXFMerger("Combined_Drawing");
//
//            // 오프셋 간격 설정
//            merger.setOffsetIncrement(500);
//
//            // 방법 1: 여러 파일 한번에 병합
//            List<String> files = Arrays.asList(
//                    "drawing1.dxf",
//                    "drawing2.dxf",
//                    "drawing3.dxf"
//            );
//
//            merger.mergeFiles(files, "merged_output.dxf");
//
//            // 방법 2: 개별 파일 추가
//            JDXFMerger merger2 = new JDXFMerger("Individual_Merge");
//            merger2.addDXFFile("part1.dxf", "Parts");
//            merger2.addDXFFile("part2.dxf", "Assembly");
//
//            // 방법 3: 사용자 정의 그리기 추가
//            merger2.addCustomDrawing("Custom_Layer", (graphics) -> {
//                graphics.setColor(Color.BLUE);
//                graphics.setStroke(new BasicStroke(3));
//                graphics.drawRect(100, 100, 200, 150);
//                graphics.drawString("Custom Drawing", 110, 180);
//            });
//
//            merger2.saveToFile("individual_merge.dxf");
//
//            System.out.println("DXF 병합 작업이 완료되었습니다!");
//
//        } catch (IOException e) {
//            System.err.println("오류 발생: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}