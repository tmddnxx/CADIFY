package com.cadify.cadifyWAS.repository;

import com.cadify.cadifyWAS.model.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderKey(String ordersKey);

    Optional<Payment> findByPaymentKey(String paymentKey);

    Optional<Payment> findByTid(String orderId);

    void deleteByOrderKey(String orderKey);
}
