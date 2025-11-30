package com.cadify.cadifyWAS.service.file.enumValues.cnc.defaultValue;

import com.cadify.cadifyWAS.service.file.enumValues.metal.defaultValue.MetalHoleSize;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CNCHoleSize {
    M2("M2", 1.6),
    M2_5("M2.5", 2.05),
    M3("M3", 2.5),
    M3_5("M3.5", 2.9),
    M4("M4", 3.3),
    M5("M5", 4.2),
    M6("M6", 5.0),
    M7("M7", 6.0),
    M8("M8", 6.75),
    M10("M10", 8.5),
    M12("M12", 10.25),
    M14("M14", 12.0),
    M16("M16", 14.0),
    M18("M18", 15.5),
    M20("M20", 17.5),
    M22("M22", 19.5),
    M24("M24", 21.0),
    M27("M27", 24.0),
    M30("M30", 26.5),
    M33("M33", 29.5),
    M36("M36", 32.0),
    M39("M39", 35.0),
    M42("M42", 37.5),
    M45("M45", 40.5),
    M48("M48", 43.0),
    M52("M52", 47.0),
    M56("M56", 50.5),
    M60("M60", 54.5),
    M64("M64", 58.0),
    M68("M68", 62.0),
    M72("M72", 66.0),
    M76("M76", 70.0),
    M80("M80", 74.0),
    M85("M85", 79.0),
    M90("M90", 84.0),
    M95("M95", 89.0),
    M100("M100", 94.0);

    private final String type;
    private final double diameter;

    public static String getTypeByHoleSize(double diameter) {
        for (CNCHoleSize holeSize : CNCHoleSize.values()) {
            if (holeSize.getDiameter() == diameter) {
                return holeSize.getType();
            }
        }
        return "hole";
    }

    public static boolean isValidHoleSize(String type, double diameter) {
        for (CNCHoleSize holeSize : CNCHoleSize.values()) {
            if (holeSize.getType().equals(type) && holeSize.getDiameter() == diameter) {
                return true;
            }
        }
        return false;
    }
}
