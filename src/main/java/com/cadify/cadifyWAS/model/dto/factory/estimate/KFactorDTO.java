package com.cadify.cadifyWAS.model.dto.factory.estimate;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

public class KFactorDTO {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Upsert {
        private String material; // 재질
        private Double thickness; // 두께
        @JsonProperty("kFactor")
        private Double kFactor; // KFactor 값
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String material; // 재질
        private Double thickness; // 두께
        private Double kFactor; // KFactor 값
    }
}