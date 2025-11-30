package com.cadify.cadifyWAS.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class ErrorResponse {
    private final Integer status;
    private final String code;
    private final String message;
    private final Instant occurredAt;

    // 생성자 (@Builder가 생성해주지만, 직접 생성자 만들 수도 있음)
    public ErrorResponse(Integer status, String code, String message, Instant occurredAt) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.occurredAt = occurredAt;
    }

    public static ErrorResponse of(CustomLogicException exception) {
        ExceptionCode code = exception.getExceptionCode();
        return ErrorResponse.builder()
                .status(code.getStatus())
                .code(code.name())
                .message(exception.getMessage())
                .occurredAt(exception.getOccurredAt())
                .build();
    }
}