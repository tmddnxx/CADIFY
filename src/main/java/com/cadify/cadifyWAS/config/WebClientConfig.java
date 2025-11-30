package com.cadify.cadifyWAS.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClientNicePayment() {
        return WebClient.builder()
                .baseUrl("https://api.nicepay.co.kr/v1/payments")
                .build();
    }

    @Bean
    public WebClient webClientNicePaymentTest() {
        return WebClient.builder()
                .baseUrl("https://sandbox-api.nicepay.co.kr/v1/payments")
                .build();
    }
}
