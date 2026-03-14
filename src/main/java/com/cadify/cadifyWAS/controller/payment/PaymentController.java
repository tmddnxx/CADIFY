package com.cadify.cadifyWAS.controller.payment;

import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.model.dto.order.OrdersDTO;
import com.cadify.cadifyWAS.model.dto.payment.InvoiceDto;
import com.cadify.cadifyWAS.model.dto.payment.PaymentDTO;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.orchestrator.PaymentEstimateFacade;
import com.cadify.cadifyWAS.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentEstimateFacade paymentEstimateFacade;

    // 결제 검증
    @PostMapping("/confirm")
    public ResponseEntity<OrdersDTO.SuccessResponse> confirmPayment(@Valid @RequestBody PaymentDTO.Confirm confirmRequest) {
        List<EstimateDTO.EstimateValidStatus> estimateValidStatusList = paymentEstimateFacade.getEstimateValidStatusList(confirmRequest.getEstKeys());
        return ResponseEntity.ok(paymentService.confirmPayment(estimateValidStatusList, confirmRequest));
    }

    // 거래명세서 조회(뽑기)
    @PostMapping("/transaction_statement")
    public ResponseEntity<byte[]> generateInvoice(@Valid @RequestBody InvoiceDto.Request request) {
        byte[] pdfBytes = paymentService.generateInvoicePdf(request);

        String fileName = URLEncoder.encode("invoice.pdf", StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice.pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    //카드 매출전표 뽑기
    @GetMapping("/receipt/{orderKey}")
    public ResponseEntity<PaymentDTO.ReceiptResponse> downloadTossReceipt(@PathVariable("orderKey") String orderKey){
        return ResponseEntity.ok().body(paymentService.downloadReceipt(orderKey));
    }

    // 결제 취소
    @PostMapping("/{tid}/cancel")
    public ResponseEntity<ResultResponse> cancelPaymentByUser(@PathVariable String tid, @Valid @RequestBody PaymentDTO.CancelRequest cancelRequest) {
        paymentService.cancelPaymentByUser(tid, cancelRequest);
        return ResponseEntity.ok().body(ResultResponse.of(ResultCode.PAYMENT_CANCEL_USER_SUCCESS));
    }

    //망 취소
    @PostMapping("/cancel/network")
    public ResponseEntity<ResultResponse> cancelPaymentByNetwork(@Valid @RequestBody PaymentDTO.CancelRequestByNetwork cancelRequest) {
        paymentService.cancelPaymentByNetwork(cancelRequest);
        return ResponseEntity.ok().body(ResultResponse.of(ResultCode.PAYMENT_CANCEL_BY_NETWORK_SUCCESS));
    }

    //거래 조회
    @GetMapping("/{tid}")
    public ResponseEntity<PaymentDTO.Response> getPayment(@PathVariable String tid) {
        return ResponseEntity.ok().body(paymentService.getPayment(tid));
    }

}
