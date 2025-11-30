package com.cadify.cadifyWAS.service.file.enumValues.cnc.limitValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CNCEndMillSize {
    STEEL_1("STEEL", 1.499, 1.998, 6),
    STEEL_2("STEEL", 1.999, 2.498, 8),
    STEEL_3("STEEL", 2.499, 2.998, 14),
    STEEL_4("STEEL", 2.999, 3.5, 25),
    STEEL_5("STEEL", 3.5, 4.5, 30),
    STEEL_6("STEEL", 4.5, 6.0, 40),
    STEEL_7("STEEL", 6.0, 7.0, 45),
    STEEL_8("STEEL", 7.0, 9.0, 50),
    STEEL_9("STEEL", 9.0, 11.0, 60),
    STEEL_10("STEEL", 11.0, 13.0, 70),
    STEEL_11("STEEL", 13.0, 17.0, 120),
    STEEL_12("STEEL", 17.0, Double.MAX_VALUE, 120),

    NON_STEEL_1("NON_STEEL", 1.499, 1.998, 6),
    NON_STEEL_2("NON_STEEL", 1.999, 2.498, 16),
    NON_STEEL_3("NON_STEEL", 2.499, 2.998, 16),
    NON_STEEL_4("NON_STEEL", 2.999, 3.5, 20),
    NON_STEEL_5("NON_STEEL", 3.5, 4.5, 30),
    NON_STEEL_6("NON_STEEL", 4.5, 6.0, 30),
    NON_STEEL_7("NON_STEEL", 6.0, 7.0, 45),
    NON_STEEL_8("NON_STEEL", 7.0, 9.0, 50),
    NON_STEEL_9("NON_STEEL", 9.0, 11.0, 65),
    NON_STEEL_10("NON_STEEL", 11.0, 13.0, 65),
    NON_STEEL_11("NON_STEEL", 13.0, 17.0, 100),
    NON_STEEL_12("NON_STEEL", 17.0, Double.MAX_VALUE, 150),

    STAIN_1("STAIN", 1.499, 1.998, 6),
    STAIN_2("STAIN", 1.999, 2.498, 8),
    STAIN_3("STAIN", 2.499, 2.998, 14),
    STAIN_4("STAIN", 2.999, 3.5, 14),
    STAIN_5("STAIN", 3.5, 4.5, 30),
    STAIN_6("STAIN", 4.5, 6.0, 30),
    STAIN_7("STAIN", 6.0, 7.0, 30),
    STAIN_8("STAIN", 7.0, 9.0, 40),
    STAIN_9("STAIN", 9.0, 11.0, 50),
    STAIN_10("STAIN", 11.0, 13.0, 60),
    STAIN_11("STAIN", 13.0, 17.0, 70),
    STAIN_12("STAIN", 17.0, Double.MAX_VALUE, 90),

    RESIN_1("RESIN", 1.499, 1.998, 6),
    RESIN_2("RESIN", 1.999, 2.498, 8),
    RESIN_3("RESIN", 2.499, 2.998, 14),
    RESIN_4("RESIN", 2.999, 3.5, 25),
    RESIN_5("RESIN", 3.5, 4.5, 30),
    RESIN_6("RESIN", 4.5, 6.0, 40),
    RESIN_7("RESIN", 6.0, 7.0, 45),
    RESIN_8("RESIN", 7.0, 9.0, 50),
    RESIN_9("RESIN", 9.0, 11.0, 60),
    RESIN_10("RESIN", 11.0, 13.0, 70),
    RESIN_11("RESIN", 13.0, 17.0, 120),
    RESIN_12("RESIN", 17.0, Double.MAX_VALUE, 120);

    private final String material;
    private final double minDiameter;
    private final double maxDiameter;
    private final double depthLimit;

    // 재질에 따른 최대 깊이 제한
    public static boolean isValidDepthByDiameter(String material, double diameter, double depth) {
        boolean hasMaterial = Arrays.stream(CNCEndMillSize.values())
                .anyMatch(size -> size.getMaterial().equals(material));

        if (!hasMaterial) {
            throw new IllegalArgumentException("잘못된 재질입니다. \n재질 : " + material);
        }

        return Arrays.stream(CNCEndMillSize.values())
                .filter(size -> size.getMaterial().equals(material))
                .filter(size -> size.getMinDiameter() < diameter && diameter <= size.getMaxDiameter())
                .findFirst()
                .map(size -> depth <= size.getDepthLimit())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 재질입니다. \n재질 : " + material));
    }

    // 직경에 따른 최대 깊이 찾기
    public static double getDepthLimitByDiameter(String material, double diameter) {
        return Arrays.stream(CNCEndMillSize.values())
                .filter(size -> size.getMaterial().equals(material))
                .filter(size -> size.getMinDiameter() < diameter && diameter <= size.getMaxDiameter())
                .map(CNCEndMillSize::getDepthLimit)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 재질입니다. \n재질 : " + material));
    }

}
