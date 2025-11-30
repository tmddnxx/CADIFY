package com.cadify.cadifyWAS.service.file.enumValues.cnc.limitValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CNCDrillHoleSize {

    D2(2.0), D2_5(2.5), D3(3.0), D3_5(3.5), D4(4.0), D4_5(4.5), D5(5.0), D5_5(5.5),
    D6(6.0), D6_5(6.5), D7(7.0), D7_5(7.5), D8(8.0), D8_5(8.5), D9(9.0), D9_5(9.5),
    D10(10.0), D11(11.0), D12(12.0), D13(13.0), D14(14.0), D15(15.0), D16(16.0),
    D17(17.0), D18(18.0), D19(19.0), D20(20.0), D21(21.0), D22(22.0), D23(23.0),
    D24(24.0), D25(25.0), D26(26.0), D27(27.0), D28(28.0), D29(29.0), D30(30.0);

    private final double value;

    public static boolean isDrilled(double diameter) {
        return Arrays.stream(CNCDrillHoleSize.values())
                .anyMatch(d -> Double.compare(d.getValue(), diameter) == 0);
    }
}
