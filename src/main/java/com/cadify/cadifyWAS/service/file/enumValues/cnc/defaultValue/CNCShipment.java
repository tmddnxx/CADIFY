package com.cadify.cadifyWAS.service.file.enumValues.cnc.defaultValue;

import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public enum CNCShipment {

    /**
     *  선반 계
     */
    /* 스틸 계 */
    SM45C_NA_LATHE("SM45C", "NA", 6, 3, "lathe"), // 없음
    SS400_NA_LATHE("SS400", "NA", 6, 3, "lathe"), // 없음
    SKD11_NA_LATHE("SKD11", "NA", 6, 3, "lathe"), // 없음

    SM45C_ENI_LATHE("SM45C", "ENI", 8, 5, "lathe"), // 무전해 니켈도금
    SS400_ENI_LATHE("SS400", "ENI", 8, 5, "lathe"), // 무전해 니켈도금
    SKD11_ENI_LATHE("SKD11", "ENI", 8, 5, "lathe"), // 무전해 니켈도금

    SM45C_BLO_LATHE("SM45C", "BLO", 8, 5, "lathe"), // 흑착색
    SS400_BLO_LATHE("SS400", "BLO", 8, 5, "lathe"), // 흑착색
    SKD11_BLO_LATHE("SKD11", "BLO", 8, 5, "lathe"), // 흑착색

    SM45C_HCR_LATHE("SM45C", "HCR", 8, 5, "lathe"), // 경질 크롬 도금
    SS400_HCR_LATHE("SS400", "HCR", 8, 5, "lathe"), // 경질 크롬 도금
    SKD11_HCR_LATHE("SKD11", "HCR", 8, 5, "lathe"), // 경질 크롬 도금

    SM45C_NIC_LATHE("SM45C", "NIC", 8, 5, "lathe"), // 니켈 도금
    SS400_NIC_LATHE("SS400", "NIC", 8, 5, "lathe"), // 니켈 도금
    SKD11_NIC_LATHE("SKD11", "NIC", 8, 5, "lathe"), // 니켈 도금

    SM45C_CHR_LATHE("SM45C", "CHR", 8, 5, "lathe"), // 크롬 도금
    SS400_CHR_LATHE("SS400", "CHR", 8, 5, "lathe"), // 크롬 도금
    SKD11_CHR_LATHE("SKD11", "CHR", 8, 5, "lathe"), // 크롬 도금

    /* 스테인리스 계 */
    SUS303_NA_LATHE("SUS303", "NA", 6, 3, "lathe"), // 없음
    SUS304_NA_LATHE("SUS304", "NA", 6, 3, "lathe"), // 없음
    SUS316_NA_LATHE("SUS316", "NA", 6, 3, "lathe"), // 없음

    /* 알류미늄 계 */
    AL6061_NA_LATHE("AL6061", "NA", 6, 3, "lathe"), // 없음
    AL5052_NA_LATHE("AL5052", "NA", 6, 3, "lathe"), // 없음
    AL7075_NA_LATHE("AL7075", "NA", 6, 3, "lathe"), // 없음

    AL6061_WAS_LATHE("AL6061", "WAS", 8, 5, "lathe"), // 백색 아노다이징(반광)
    AL5052_WAS_LATHE("AL5052", "WAS", 8, 5, "lathe"), // 백색 아노다이징(반광)
    AL7075_WAS_LATHE("AL7075", "WAS", 8, 5, "lathe"), // 백색 아노다이징(반광)

    AL6061_WAM_LATHE("AL6061", "WAM", 8, 5, "lathe"), // 백색 아노다이징(무광)
    AL5052_WAM_LATHE("AL5052", "WAM", 8, 5, "lathe"), // 백색 아노다이징(무광)
    AL7075_WAM_LATHE("AL7075", "WAM", 8, 5, "lathe"), // 백색 아노다이징(무광)

    AL6061_BAS_LATHE("AL6061", "BAS", 8, 5, "lathe"), // 흑색 아노다이징(반광)
    AL5052_BAS_LATHE("AL5052", "BAS", 8, 5, "lathe"), // 흑색 아노다이징(반광)
    AL7075_BAS_LATHE("AL7075", "BAS", 8, 5, "lathe"), // 흑색 아노다이징(반광)

    AL6061_BAM_LATHE("AL6061", "BAM", 8, 5, "lathe"), // 흑색 아노다이징(무광)
    AL5052_BAM_LATHE("AL5052", "BAM", 8, 5, "lathe"), // 흑색 아노다이징(무광)
    AL7075_BAM_LATHE("AL7075", "BAM", 8, 5, "lathe"), // 흑색 아노다이징(무광)

    AL6061_HAM_LATHE("AL6061", "HAM", 8, 5, "lathe"), // 경질 아노다이징(국방)
    AL5052_HAM_LATHE("AL5052", "HAM", 8, 5, "lathe"), // 경질 아노다이징(국방)
    AL7075_HAM_LATHE("AL7075", "HAM", 8, 5, "lathe"), // 경질 아노다이징(국방)

    /* 수지 계 */
    POM_WHITE_NA_LATHE("POM_WHITE", "NA", 6, 0, "lathe"), // 없음
    POM_BLACK_NA_LATHE("POM_BLACK", "NA", 6, 0, "lathe"), // 없음
    MC_NYLON_BLUE_NA_LATHE("MC_NYLON_BLUE", "NA", 6, 0, "lathe"), // 없음
    MC_NYLON_IVORY_NA_LATHE("MC_NYLON_IVORY", "NA", 6, 0, "lathe"), // 없음
    PEEK_NA_LATHE("PEEK", "NA", 6, 0, "lathe"), // 없음
    
    /* 황동 */
    BRASS_C3604_NA_LATHE("BRASS_C3604", "NA", 6, 0, "lathe"), // 없음

    /**
     * 밀링 계
     */
    SM45C_NA_MILLING("SM45C", "NA", 6, 0, "milling"),
    SS400_NA_MILLING("SS400", "NA", 6, 0, "milling"),
    SKD11_NA_MILLING("SKD11", "NA", 6, 0, "milling"),

    SM45C_ENI_MILLING("SM45C", "ENI", 9, 0, "milling"),
    SS400_ENI_MILLING("SS400", "ENI", 9, 0, "milling"),
    SKD11_ENI_MILLING("SKD11", "ENI", 9, 0, "milling"),

    SM45C_BLO_MILLING("SM45C", "BLO", 9, 0, "milling"),
    SS400_BLO_MILLING("SS400", "BLO", 9, 0, "milling"),
    SKD11_BLO_MILLING("SKD11", "BLO", 9, 0, "milling"),

    SM45C_HCR_MILLING("SM45C", "HCR", 9, 0, "milling"),
    SS400_HCR_MILLING("SS400", "HCR", 9, 0, "milling"),
    SKD11_HCR_MILLING("SKD11", "HCR", 9, 0, "milling"),

    SM45C_NIC_MILLING("SM45C", "NIC", 9, 0, "milling"),
    SS400_NIC_MILLING("SS400", "NIC", 9, 0, "milling"),
    SKD11_NIC_MILLING("SKD11", "NIC", 9, 0, "milling"),

    SM45C_CHR_MILLING("SM45C", "CHR", 9, 0, "milling"),
    SS400_CHR_MILLING("SS400", "CHR", 9, 0, "milling"),
    SKD11_CHR_MILLING("SKD11", "CHR", 9, 0, "milling"),

    SUS303_NA_MILLING("SUS303", "NA", 7, 0, "milling"),
    SUS304_NA_MILLING("SUS304", "NA", 7, 0, "milling"),
    SUS316_NA_MILLING("SUS316", "NA", 7, 0, "milling"),

    AL6061_NA_MILLING("AL6061", "NA", 5, 0, "milling"),
    AL5052_NA_MILLING("AL5052", "NA", 5, 0, "milling"),
    AL7075_NA_MILLING("AL7075", "NA", 5, 0, "milling"),

    AL6061_WAS_MILLING("AL6061", "WAS", 8, 0, "milling"),
    AL5052_WAS_MILLING("AL5052", "WAS", 8, 0, "milling"),
    AL7075_WAS_MILLING("AL7075", "WAS", 8, 0, "milling"),

    AL6061_WAM_MILLING("AL6061", "WAM", 8, 0, "milling"),
    AL5052_WAM_MILLING("AL5052", "WAM", 8, 0, "milling"),
    AL7075_WAM_MILLING("AL7075", "WAM", 8, 0, "milling"),

    AL6061_BAS_MILLING("AL6061", "BAS", 8, 0, "milling"),
    AL5052_BAS_MILLING("AL5052", "BAS", 8, 0, "milling"),
    AL7075_BAS_MILLING("AL7075", "BAS", 8, 0, "milling"),

    AL6061_BAM_MILLING("AL6061", "BAM", 8, 0, "milling"),
    AL5052_BAM_MILLING("AL5052", "BAM", 8, 0, "milling"),
    AL7075_BAM_MILLING("AL7075", "BAM", 8, 0, "milling"),

    AL6061_HAM_MILLING("AL6061", "HAM", 8, 0, "milling"),
    AL5052_HAM_MILLING("AL5052", "HAM", 8, 0, "milling"),
    AL7075_HAM_MILLING("AL7075", "HAM", 8, 0, "milling"),

    POM_WHITE_NA_MILLING("POM_WHITE", "NA", 5, 0, "milling"),
    POM_BLACK_NA_MILLING("POM_BLACK", "NA", 5, 0, "milling"),
    MC_NYLON_BLUE_NA_MILLING("MC_NYLON_BLUE", "NA", 5, 0, "milling"),
    MC_NYLON_IVORY_NA_MILLING("MC_NYLON_IVORY", "NA", 5, 0, "milling"),
    PEEK_NA_MILLING("PEEK", "NA", 5, 0, "milling"),

    BRASS_C3604_NA_MILLING("BRASS_C3604", "NA", 5, 0, "milling")
    ;

    private final String material;
    private final String surface;
    private final int standard;
    private final int express;
    private final String type; // lathe or milling

    @AllArgsConstructor
    public enum BaseShipmentTime {
        BASE_SHIPMENT_TIME(LocalTime.of(14, 0));

        private final LocalTime time;

        public LocalDateTime getBaseTime() {
            return LocalDateTime.of(LocalDate.now(), time);
        }
    }

    public static OptionDTO.ShipmentDayDTO getShipmentDay(String material, String surface, String type) {
        for (CNCShipment shipment : CNCShipment.values()) {
            if (shipment.getMaterial().equals(material) && shipment.getSurface().equals(surface) && shipment.getType().equals(type)) {
                return OptionDTO.ShipmentDayDTO.builder()
                        .standardDay(shipment.getStandard())
                        .expressDay(shipment.getExpress())
                        .build();
            }
        }

        throw new IllegalArgumentException(
                String.format("해당하는 옵션 조합이 존재하지 않습니다. 재질: %s, 표면처리: %s, 타입: %s", material, surface, type)
        );
    }


}
