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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebHookService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;

    @Value("${payment.nicepay.secret_api_key}")
    String secretKey;

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
