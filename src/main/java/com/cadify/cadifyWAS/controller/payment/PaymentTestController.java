package com.cadify.cadifyWAS.controller.payment;

import com.cadify.cadifyWAS.model.dto.payment.PaymentTestDto;
import com.cadify.cadifyWAS.service.payment.PaymentTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test/payment")
@RequiredArgsConstructor
public class PaymentTestController {

    private final PaymentTestService paymentTestService;

    @PostMapping("/confirm/{tid}")
    public ResponseEntity<PaymentTestDto.ConfirmResponse> testConfirm(@RequestParam String tid){
        return ResponseEntity.ok().body(paymentTestService.testConfirm(tid));
    }

    @GetMapping("/{tid}")
    public ResponseEntity<PaymentTestDto.TransactionQueryResponse> testdafkjad(@RequestParam String tid){
        return ResponseEntity.ok().body(paymentTestService.testQueryByTid(tid));
    }

    @PostMapping("/{tid}/cancel")
    public ResponseEntity<PaymentTestDto.CancelResponse> testCancel(@RequestParam String tid){
        return ResponseEntity.ok().body(paymentTestService.testCancel(tid));
    }

}
