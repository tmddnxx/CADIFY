package com.cadify.cadifyWAS.model.dto.factory.order;

import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class OrderResponse {
    private String date;
    private String orderKey;
    private String name;
    private String type;
    private Long fileCnt;
    private String shipmentDate;
    private Integer totalPrice;
    private String status;
    private String modifiedAt;

    private static final Map<String, String> METHOD_KOR_NAME = Map.of(
            "SHEET_METAL", "판금",
            "CNC", "절삭"
    );

    public OrderResponse(LocalDateTime createdAt, String orderKey, String name, String type, Long fileCnt, LocalDate shipmentDate, Integer totalPrice, OrderReceivedStatus status, LocalDateTime modifiedAt){
        this.date = createdAt.toLocalDate().format(DateTimeFormatter.ISO_DATE);
        this.orderKey = orderKey;
        this.name = name;
        this.fileCnt = fileCnt;
        this.shipmentDate = shipmentDate.format(DateTimeFormatter.ISO_DATE);
        this.totalPrice = totalPrice;
        this.status = status.name();
        // + 구분자 문자열을 분리해서 한글명으로 변환 후 재조합
        this.type = Arrays.stream(type.split(","))
                .map(String::toUpperCase)
                .map(METHOD_KOR_NAME::get)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.joining("+"));
        this.modifiedAt = modifiedAt.toLocalDate().format(DateTimeFormatter.ISO_DATE);
    }
}
