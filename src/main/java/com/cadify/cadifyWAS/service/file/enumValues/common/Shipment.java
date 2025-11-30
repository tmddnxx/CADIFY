package com.cadify.cadifyWAS.service.file.enumValues.common;

import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import com.cadify.cadifyWAS.service.file.enumValues.cnc.defaultValue.CNCShipment;
import com.cadify.cadifyWAS.service.file.enumValues.metal.defaultValue.MetalShipment;
import com.cadify.cadifyWAS.util.api.HolidayAPI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
public class Shipment {

    /**
     * 납기방법 따른 실제 날짜 계산 (판금)
     * @param day  납기일
     * @return {Obejct} : 실제 날짜 ex) 2025-05-07
     */
    public static LocalDate getShipmentDateByMetal (Integer day) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseShipmentTime = MetalShipment.BaseShipmentTime.BASE_SHIPMENT_TIME.getBaseTime();

        LocalDate baseDate = now.isAfter(baseShipmentTime)
                ? now.toLocalDate().plusDays(1)
                : now.toLocalDate();

        while (HolidayAPI.isHoliday(baseDate)) {
            baseDate = baseDate.plusDays(1);
        }

        return calculateBusinessDate(baseDate, day);
    }
    
    // 납기방법 따른 일 수 (판금)
    public static OptionDTO.ShipmentDayDTO getShipmentDayByMetal(String material, String surface) {
        return MetalShipment.getShipmentDay(material, surface);
    }

    /**
     * 납기방법 따른 실제 날짜 계산 (절삭)
     * @param day  납기일
     * @return {Obejct} : 실제 날짜 ex) 2025-05-07
     */
    public static LocalDate getShipmentDateByCNC(Integer day) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseShipmentTime = CNCShipment.BaseShipmentTime.BASE_SHIPMENT_TIME.getBaseTime();

        LocalDate baseDate = now.isAfter(baseShipmentTime)
                ? now.toLocalDate().plusDays(1)
                : now.toLocalDate();

        while (HolidayAPI.isHoliday(baseDate)) {
            baseDate = baseDate.plusDays(1);
        }

        return calculateBusinessDate(baseDate, day);
    }


    public static OptionDTO.ShipmentDayDTO getShipmentDayByCNC(String material, String surface, String type) {
        if (type.toLowerCase().contains("milling")) {
            type = "milling";
        } else {
            type = "lathe";
        }
        return CNCShipment.getShipmentDay(material, surface, type);
    }

    private static LocalDate calculateBusinessDate(LocalDate startDate, int businessDays) {
        LocalDate result = startDate;
        int count = 0;

        if (businessDays == 0) return null;

        while (count < businessDays) {
            result = result.plusDays(1);
            if (!HolidayAPI.isHoliday(result)) {
                count++;
            }
        }

        return result;
    }
}
