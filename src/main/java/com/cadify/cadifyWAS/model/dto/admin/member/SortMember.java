//package com.cadify.cadifyWAS.model.dto.admin.member;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.querydsl.core.types.OrderSpecifier;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//
//import java.util.Arrays;
//
//@RequiredArgsConstructor
//@Getter
//public enum SortMember {
//    ORDER_COUNT("orderCount"),
//    AMOUNT("amount"),
//    JOINED("joined"),
//    NAME("name");
//
//    private final String value;
//
//    // 역 직렬화 메서드
//    @JsonCreator
//    public static SortMember from(String value){
//        return Arrays.stream(SortMember.values())
//                .filter(sortMember -> sortMember.value.equals(value))
//                .findFirst()
//                .orElseThrow(() -> new IllegalArgumentException("Invalid sortBy: " + value));
//    }
//}
