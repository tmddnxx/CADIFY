package com.cadify.cadifyWAS.service.payment;

import com.cadify.cadifyWAS.model.dto.payment.PaymentDTO;
import com.cadify.cadifyWAS.model.dto.payment.PaymentTestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentTestService {

    private final WebClient webClientNicePaymentTest;

    @Value("${payment.nicepay.test.secret_api_key}")
    String secretKey;

    @Value("${payment.nicepay.test.client_api_key}")
    String clientKey;


    public PaymentTestDto.ConfirmResponse testConfirm(String tid) { // 반환 타입을 Mono로 변경
         return webClientNicePaymentTest.post()
                .uri("/v1/payments/{tid}", tid) // **URI 경로 수정**
                .header("Authorization", getAuthorization())
                .header("Content-Type", "application/json;charset=utf-8")
                .bodyValue(Map.of("amount", 1004)) // 예시 금액
                .retrieve()
                // HTTP 상태 코드 4xx, 5xx 에러 처리
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    return Mono.error(new RuntimeException("NicePay API Error: " + errorBody));
                                }))
                .bodyToMono(PaymentTestDto.ConfirmResponse.class) // 응답 DTO로 변환
                .block();// **여기서 블로킹 발생**

    }

    public PaymentTestDto.TransactionQueryResponse testQueryByTid(String tid) {

         return webClientNicePaymentTest.get() // GET 메서드
                .uri("/v1/payments/{tid}", tid) // TID를 이용한 조회 API URI
                .header("Authorization", getAuthorization()) // GET 요청에도 Authorization 필요
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    return Mono.error(new RuntimeException("NicePay API Error: " + errorBody));
                                }))
                .bodyToMono(PaymentTestDto.TransactionQueryResponse.class)
                .block(); // 블로킹
    }

    public PaymentTestDto.CancelResponse testCancel(String tid) { // cancelAmount 파라미터 제거

        return webClientNicePaymentTest.post()
                .uri("/v1/payments/{tid}/cancel", tid)
                .header("Authorization", getAuthorization())
                // 취소 요청 본문이 없을 경우 Content-Type을 생략해도 되지만,
                // 명시적으로 "application/json;charset=utf-8"을 유지하는 것이 좋습니다.
                .header("Content-Type", "application/json;charset=utf-8")
                // .bodyValue(Map.of("cancelAmount", totalAmount)) // 만약 특정 금액(전체 금액)을 보내야 한다면 이 주석을 해제
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    return Mono.error(new RuntimeException("NicePay API Error: " + errorBody));
                                }))
                .bodyToMono(PaymentTestDto.CancelResponse.class)
                .block();


    }

    private String getAuthorization() {
        String credentials = clientKey + ":" + secretKey; // 클라이언트 아이디 + 시크릿 키
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
