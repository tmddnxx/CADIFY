package com.cadify.cadifyWAS.model.dto.files;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OptionDTO {

    private String key;
    private List<OptionType> options;

    public interface OptionType {
        String getMaterial();
        List<Double> getThicknessList();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MaterialByThickness implements OptionType{ // 두께별 가능 재질 (판금)
        private String material;
        private List<Double> thicknessList;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Options implements OptionType {
        private String label;
        private Object value;
        private String type; // metal, cnc, common

        @Override
        public String getMaterial() {
            return "";
        }

        @Override
        public List<Double> getThicknessList() {
            return List.of();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @ToString
    @NoArgsConstructor
    public static class Hole {
        private List<List<Integer>> faceIds;
        private String type;
        private int count;
        private double diameter;
        private Double csDiameter;
        private Double csAngle;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BBox {
        private double x;
        private double y;
        private double z;
    }



    @Getter
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class ShipmentDayDTO {
        private Integer standardDay;
        private Integer expressDay;
    }
}
