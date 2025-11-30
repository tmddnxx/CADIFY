package com.cadify.cadifyWAS.mapper;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.model.dto.order.OrdersDTO;
import com.cadify.cadifyWAS.model.dto.order.OrderItemDTO;
import com.cadify.cadifyWAS.model.dto.payment.PaymentDTO;
import com.cadify.cadifyWAS.model.entity.Address;
import com.cadify.cadifyWAS.model.entity.member.CompanyManager;
import com.cadify.cadifyWAS.model.entity.order.OrderItem;
import com.cadify.cadifyWAS.model.entity.order.Orders;
import com.cadify.cadifyWAS.model.entity.payment.Payment;
import com.cadify.cadifyWAS.repository.AddressRepository;
import com.cadify.cadifyWAS.repository.OrderItemRepository;
import com.cadify.cadifyWAS.repository.company.CompanyManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.cadify.cadifyWAS.exception.ExceptionCode.*;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final OrderItemMapper orderItemMapper;
    private final OrderItemRepository orderItemRepository;
    private final AddressRepository addressRepository;
    private final CompanyManagerRepository managerRepository;

    /**
     * 주문 목록을 반환해야하는 메소드
     * 주문 정보, 결제 정보, 주소 정보를 모두 반환해야 함.
     * @param order
     * @return
     */
    public OrdersDTO.Response orderToOrderResponse(Orders order, Address address, Optional<Payment> nicePayment) {

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderKey(order.getOrderKey());

        List<OrderItemDTO.Response> orderItemResponses = orderItems.stream().map(
                orderItemMapper::orderItemToOrderItemResponse
        ).toList();

        Optional<CompanyManager> manager = managerRepository.findByManagerKey(order.getManagerKey());

        return OrdersDTO.Response.builder()
                .orderKey(order.getOrderKey())
                .orderItems(orderItemResponses)
                .orderAt(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .repName(manager.map(CompanyManager::getManagerName).orElse(""))
                .repPhoneNumber(manager.map(CompanyManager::getPhone).orElse(""))
                .address(address.getAddress())
                .addressDetail(address.getAddressDetail())
                .deliveryCharge(order.getDeliveryCharge())
                .paid(order.isPaid())
                .deliveryMemo(order.getDeliveryMemo())
                .vat(order.getVat())
                .nicePayment(nicePayment.map(PaymentDTO.OrderResponse::paymentToOrder).orElse(null)) // Optional 처리
                .build();
    }

    public OrdersDTO.AllResponse orderToAllOrderResponse(Orders order) {

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderKey(order.getOrderKey());

        List<OrderItemDTO.Response> orderItemResponses = orderItems.stream().map(
                orderItemMapper::orderItemToOrderItemResponse
        ).toList();

        return OrdersDTO.AllResponse.builder()
                .orderAt(order.getCreatedAt())
                .totalPrice(order.getTotalPrice())
                .orderKey(order.getOrderKey())
                .orderItems(orderItemResponses)
                .paid(order.isPaid())
                .build();
    }

    public OrdersDTO.SuccessResponse orderToOrderSuccess(Orders order, List<OrderItemDTO.Response> orderItemDTOList, Address address, PaymentDTO.SuccessResponse tossPaymentDTO) {
        return OrdersDTO.SuccessResponse.builder()
                .address(address.getAddress())
                .addressDetail(address.getAddressDetail())
                .orderItems(orderItemDTOList)
                .nicePayment(tossPaymentDTO)
                .orderAt(order.getCreatedAt())
                .orderKey(order.getOrderKey())
                .build();
    }
}
