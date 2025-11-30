package com.cadify.cadifyWAS.service.file.enumValues.metal.defaultValue;

import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public enum MetalShipment {
    /* 스틸 계 */
    SS400_NA("SS400", "NA", 4, 1), // 없음
    SS400_PWC("SS400", "PWC", 8, 5), // 분체도장
    SS400_ENI("SS400", "ENI", 7, 4), // 무전해 니켈도금
    SS400_ZIN("SS400", "ZNI", 7, 4), // 전기아연도금

    SPCC_NA("SPCC", "NA", 4, 1), // 없음
    SPCC_PWC("SPCC", "PWC", 8, 5), // 분체도장
    SPCC_ENI("SPCC", "ENI", 7, 4), // 무전해 니켈도금
    SPCC_ZIN("SPCC", "ZNI", 7, 4), // 전기아연도금

    SPHC_NA("SPHC", "NA", 4, 1), // 없음
    SPHC_PWC("SPHC", "PWC", 8, 5), // 분체도장
    SPHC_ENI("SPHC", "ENI", 7, 4), // 무전해 니켈도금
    SPHC_ZIN("SPHC", "ZNI", 7, 4), // 전기아연도금

    SECC_NA("SECC", "NA", 4, 1), // 없음

    /* 스테인리스 계 */
    SUS304_No1_NA("SUS304_No1", "NA", 4, 1), // 없음
    SUS304_SH_NA("SUS304_SH", "NA", 4, 1), // 없음
    SUS304_SP_NA("SUS304_SP", "NA", 4, 1), // 없음

    SUS304_2B_NA("SUS304_2B", "NA", 4, 1), // 없음
    SUS304_DP_NA("SUS304_DP", "NA", 4, 1), // 없음

    SUS304_2B_EPP("SUS304_2B", "EPP", 4, 1), // 전해연마
    SUS304_DP_EPP("SUS304_DP", "EPP", 4, 1), // 전해연마

    /* 알류미늄 계 */
    AL5052_NA("AL5052", "NA", 4, 1), // 없음
    AL5052_PWC("AL5052", "PWC", 8, 5), // 분체도장
    AL5052_WAS("AL5052", "WAS", 6, 4), // 백색 아노다이징 (반광)
    AL5052_BAS("AL5052", "BAS", 6, 4), // 흑색 아노다이징 (반광)
    AL5052_BAM("AL5052", "BAM", 6, 4), // 흑색 아노다이징 (무광)
    AL5052_HAM("AL5052", "HAM", 6, 4), // 경질 아노다이징 (국방)
    ;

    private final String material;
    private final String surface;
    private final int standard;
    private final int express;

    @AllArgsConstructor
    public enum BaseShipmentTime {
        BASE_SHIPMENT_TIME(LocalTime.of(14, 0));

        private final LocalTime time;

        public LocalDateTime getBaseTime() {
            return LocalDateTime.of(LocalDate.now(), time);
        }
    }

    public static OptionDTO.ShipmentDayDTO getShipmentDay(String material, String surface) {
        for (MetalShipment shipment : MetalShipment.values()) {
            if (shipment.getMaterial().equals(material) && shipment.getSurface().equals(surface)) {
                return OptionDTO.ShipmentDayDTO.builder()
                        .standardDay(shipment.getStandard())
                        .expressDay(shipment.getExpress())
                        .build();
            }
        }

        throw new IllegalArgumentException(
                String.format("해당하는 옵션 조합이 존재하지 않습니다. 재질: %s, 표면처리: %s", material, surface)
        );
    }
}
