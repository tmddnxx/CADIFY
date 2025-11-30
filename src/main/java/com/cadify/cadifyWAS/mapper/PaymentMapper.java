package com.cadify.cadifyWAS.mapper;

import com.cadify.cadifyWAS.model.dto.payment.PaymentDTO;
import com.cadify.cadifyWAS.model.entity.payment.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

   public PaymentDTO.SuccessResponse toPaymentDTO(Payment payment) {
       return PaymentDTO.SuccessResponse.builder()
               .amount(payment.getAmount())
               .method(payment.getPayMethod())
               .build();
   }
}
