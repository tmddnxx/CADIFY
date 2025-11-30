package com.cadify.cadifyWAS.mapper;

import com.cadify.cadifyWAS.model.dto.order.OrderItemDTO;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.cadify.cadifyWAS.model.entity.cart.CartItem;
import com.cadify.cadifyWAS.model.entity.order.OrderItem;
import com.cadify.cadifyWAS.repository.Files.EstimateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {
    
    private final EstimateRepository estimateRepository;

    public OrderItem cartItemToOrderItem(CartItem cartItem, String orderKey, String memberKey) {
        return OrderItem.builder()
                .orderKey(orderKey)
                .memberKey(memberKey)

                // 견적 관련
                .estKey(cartItem.getEstKey())
                .estName(cartItem.getEstName())
                .method(cartItem.getMethod())
                .type(cartItem.getType())
                .price(cartItem.getUnitPrice())
                .cost(cartItem.getCost())
                .isFastShipment(cartItem.isFastShipment())
                .material(cartItem.getMaterial())
                .thickness(cartItem.getThickness())
                .surface(cartItem.getSurface())
                .coatingColor(cartItem.getCoatingColor())
                .isChamfer(cartItem.isChamfer())
                .kg(cartItem.getKg())
                .commonDiff(cartItem.getCommonDiff())
                .roughness(cartItem.getRoughness())
                .memo(cartItem.getMemo())
                .holeJson(cartItem.getHoleJson())
                .bbox(cartItem.getBbox())
                .policyVersion(cartItem.getPolicyVersion())
                .standardShipmentDay(cartItem.getStandardShipmentDay())
                .expressShipmentDay(cartItem.getExpressShipmentDay())

                // 파일 관련
                .fileKey(cartItem.getFileKey())
                .fileName(cartItem.getFileName())
                .s3StepAddress(cartItem.getS3StepAddress())
                .s3DxfAddress(cartItem.getS3DxfAddress())
                .factoryDxfAddress(cartItem.getFactoryDxfAddress())
                .imageAddress(cartItem.getImageAddress())
                .metaJson(cartItem.getMetaJson())

                // 주문 관련
                .amount(cartItem.getAmount())
                .unitPrice(cartItem.getUnitPrice())
                .totalPrice(cartItem.getTotalPrice())
                .discount(cartItem.getDiscount())
                .paymentPrice(cartItem.getPaymentPrice())
                .shipmentDate(cartItem.getShipmentDate())

                // 상태는 생성자에서 기본값(PAYMENT_PENDING)으로 설정됨
                .build();
    }



    public OrderItemDTO.Response orderItemToOrderItemResponse(OrderItem orderItem) {

        // 공장용: Estimate에서 cost 값 가져오기
        int cost = 0;
        int itemTotalCost = 0;
        if (orderItem.getEstKey() != null) {
            Estimate estimate = estimateRepository.findByEstKey(orderItem.getEstKey()).orElse(null);
            if (estimate != null) {
                cost = estimate.getCost();
                // cost 기반으로 총액 계산
                itemTotalCost = (int) (cost * orderItem.getAmount());
            }
        }

        return OrderItemDTO.Response.builder()
                .orderItemKey(orderItem.getOrderItemKey())
                .amount(orderItem.getAmount())
                .unitPrice(orderItem.getPrice())
                .totalPrice(orderItem.getTotalPrice())
                .discount(orderItem.getDiscount())
                .paymentPrice(orderItem.getPaymentPrice())
                .itemTotalCost(itemTotalCost) // 공장용 cost 총액 추가
                .estKey(orderItem.getEstKey())
                .estName(orderItem.getEstName())
                .material(orderItem.getMaterial())
                .orderReceivedStatus(orderItem.getOrderReceivedStatus())
                .fileName(orderItem.getFileName())
                .isFastShipment(orderItem.isFastShipment())
                .shipmentDate(orderItem.getShipmentDate())
                .surface(orderItem.getSurface())
                .method(orderItem.getMethod())
                .imageAddress(orderItem.getImageAddress())
                .trackingNumber(orderItem.getTrackingNumber())
                .courier(orderItem.getCourier())
                .build();
    }
}
