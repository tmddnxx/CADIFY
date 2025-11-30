package com.cadify.cadifyWAS.service.payment;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.infra.order.RetryOnOptimisticLock;
import com.cadify.cadifyWAS.model.dto.payment.NicePaymentWebHookDTO;
import com.cadify.cadifyWAS.model.entity.order.OrderItem;
import com.cadify.cadifyWAS.model.entity.order.Orders;
import com.cadify.cadifyWAS.model.entity.payment.Payment;
import com.cadify.cadifyWAS.repository.OrderItemRepository;
import com.cadify.cadifyWAS.repository.OrderRepository;
import com.cadify.cadifyWAS.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentWebHookService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    String secretKey;

    // 웹훅 받아서 처리
//    @Transactional
//    public void receiveWebHook(NicePaymentWebHookDTO tossWebHookDTO) throws IOException, InterruptedException {
//
//        String status = tossWebHookDTO.getData().getStatus();
//
//        String paymentKey = tossWebHookDTO.getData().getPaymentKey();
//
//        Payment tossPayment = paymentRepository.findByPaymentKey(paymentKey)
//                .orElseThrow(() -> new CustomLogicException(ExceptionCode.TOSS_PAYMENT_NOT_FOUND));
//
//        // 토스 결제 상태 업데이트 (웹 훅에서 온걸로)
//        tossPayment.updateStatus(status);
//
//        int paymentTotalAmount = getPaymentTotalAmount(tossWebHookDTO.getData().getPaymentKey());
//
//        Orders orders = orderRepository.findOrderByOrderKey(tossPayment.getOrderKey())
//                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ORDER_NOT_FOUND));
//
//        List<OrderItem> orderItems = orderItemRepository.findAllByOrderKey(orders.getOrderKey());
//
//        // 웹훅에서 온 결제 금액과 주문 금액이 다르면 예외 처리
//        if(paymentTotalAmount != orders.getTotalPrice()){
//            throw new CustomLogicException(ExceptionCode.TOSS_PAYMENT_FAILED);
//        }
//
//        switch (status) {
//            case "DONE":
////                orders.completePayment(orderItems);
//                log.info("done");
//                break;
//            case "CANCELED":
////                orders.cancelPayment(orderItems);
//                log.info("canceled");
//                break;
//            case "ABORTED":
//                log.info("aborted");
//            case "EXPIRED":
////                orders.paymentFailed(orderItems); // 결제 실패 처리
//                log.info("expired");
//                break;
//            default:
//                log.info("처리하지 않는 상태: " + status);
//        }
//
//
//    }

    private String getAuthorizations() {
        String credentials = secretKey + ":"; // 시크릿 키 뒤에 ":" 추가
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    @Transactional
    @RetryOnOptimisticLock
    public void receiveWebHook(NicePaymentWebHookDTO.Request webhookDTO) {

        Orders order = orderRepository.findById(webhookDTO.getOrderId())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ORDER_NOT_FOUND));

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderKey(order.getOrderKey());

        // 결제 완료 상태가 날아옴
        if (webhookDTO.getStatus().equals("PAID")) {
            // 주문이 결제 안되어있을 때
            if (!order.isPaid()) {
                updatePayment(webhookDTO);
                order.successPaid(orderItems);
            }

        } else if (webhookDTO.getStatus().equals("CANCELED")) {
            if(!order.isPaid()) {
                updatePayment(webhookDTO);
                order.updateStatusToCanceled(orderItems);
            }
        } else if (webhookDTO.getStatus().equals("FAILED")) {
            updatePayment(webhookDTO);
            order.updateStatusToPaymentPending(orderItems);
        }

    }

    private void updatePayment(NicePaymentWebHookDTO.Request webhookDTO) {
        Payment payment = paymentRepository.findByOrderKey(webhookDTO.getOrderId())
                .orElseGet(() -> Payment.builder()
                        .orderKey(webhookDTO.getOrderId())
                        .build());

        payment.updateByWebHook(webhookDTO);
    }
}
