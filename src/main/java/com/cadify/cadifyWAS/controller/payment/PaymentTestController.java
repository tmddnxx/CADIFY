package com.cadify.cadifyWAS.controller.payment;

import com.cadify.cadifyWAS.model.dto.payment.PaymentTestDto;
import com.cadify.cadifyWAS.service.payment.PaymentTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
