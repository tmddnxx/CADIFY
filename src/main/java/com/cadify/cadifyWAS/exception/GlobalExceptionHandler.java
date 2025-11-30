package com.cadify.cadifyWAS.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 로직 수행 중 발생 에러
    @ExceptionHandler(CustomLogicException.class)
    public ResponseEntity<ErrorResponse> handleCustomExceptions(CustomLogicException exception){
        ErrorResponse response = ErrorResponse.of(exception);

        // 커스텀 에러 정보 출력
        logger.error("""
        \n🔴 [API ERROR OCCURRED]
        ▶️ STATUS   : {}
        ▶️ EXCEPTION : {}
        ▶️ MESSAGE : {}
        """, response.getStatus(), response.getCode(), response.getMessage());

        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleUnKnownExceptions(RuntimeException exception){
        logger.error("알 수 없는 에러 :", exception);

        ErrorResponse response = ErrorResponse.of(
                new CustomLogicException(ExceptionCode.UNKNOWN_EXCEPTION_OCCURED, exception.getMessage())
        );

        return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        // 모든 필드 에러 메시지를 리스트로 변환
        List<String> errorMessages = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        // 여러 개의 에러 메시지를 하나의 문자열로 조합 (줄바꿈 추가)
        String combinedMessages = String.join(", ", errorMessages);

        // `ErrorResponse` 형식으로 응답 생성
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("VALIDATION_FAILED")  // 예외 유형을 명확히 하기 위한 코드
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .message(combinedMessages) // 모든 필드 에러 메시지 포함
                .occurredAt(Instant.now()) // 발생 시간
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }
}