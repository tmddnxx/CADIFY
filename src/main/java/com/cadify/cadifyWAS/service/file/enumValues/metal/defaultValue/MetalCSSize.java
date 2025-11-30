package com.cadify.cadifyWAS.service.file.enumValues.metal.defaultValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MetalCSSize {

    CS_M2("CS_M2",2.40, 2.54, 4.4, 4.5),
    CS_M2_5("CS_M2.5",2.90, 3.04, 5.5, 5.6),
    CS_M3("CS_M3",3.40, 3.58, 6.3, 6.5),
    CS_M4("CS_M4",4.50, 4.68, 9.4, 9.6),
    CS_M5("CS_M5",5.50, 5.68, 10.4, 10.65),
    CS_M6("CS_M6",6.60, 6.82, 12.6, 12.85),
    CS_M8("CS_M8",9.00, 9.22, 17.3, 17.55),
    CS_M10("CS_M10",11.00, 11.27, 20.0, 20.3);

    private final String type; // 홀타입
    private final double baseMin;     // 기초홀 최소
    private final double baseMax;     // 기초홀 최대
    private final double sinkMin;     // 접시 최소
    private final double sinkMax;     // 접시 최대

    public static String getTypeByCounterSink(double diameter, double csDiameter) {
        for (MetalCSSize csSize : MetalCSSize.values()) {
            if (csSize.getBaseMin() <= diameter && diameter <= csSize.getBaseMax() &&
                    csSize.getSinkMin() <= csDiameter && csDiameter <= csSize.getSinkMax()) {
                return csSize.getType();
            }
        }
        return null;
    }
}
