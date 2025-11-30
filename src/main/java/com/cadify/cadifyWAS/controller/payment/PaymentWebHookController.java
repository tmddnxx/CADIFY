package com.cadify.cadifyWAS.controller.payment;

import com.cadify.cadifyWAS.model.dto.payment.NicePaymentWebHookDTO;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.payment.PaymentWebHookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebHookController {

    private final PaymentWebHookService paymentWebHookService;

    @PostMapping()
    public ResponseEntity<String> receiveWebHook(@RequestBody NicePaymentWebHookDTO.Request webHookRequest) throws IOException, InterruptedException {
        log.info("웹훅 API 들어옴");
        paymentWebHookService.receiveWebHook(webHookRequest);

        return ResponseEntity.ok().body("ok");
    }
}
