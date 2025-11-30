package com.cadify.cadifyWAS.mapper;

import com.cadify.cadifyWAS.model.dto.cart.CartItemDTO;
import com.cadify.cadifyWAS.model.entity.Files.Files;
import com.cadify.cadifyWAS.model.entity.cart.Cart;
import com.cadify.cadifyWAS.model.entity.cart.CartItem;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {


    public CartItem estimateToCartItem(Cart cart, Estimate estimate, Files files, int amount) {
        return CartItem.builder()
                /* ── 장바구니/사용자 정보 ────────────────────────── */
                .cartKey(cart.getCartKey())
                /* ── 견적 기반 정보 ─────────────────────────────── */
                .estKey(estimate.getEstKey())
                .estName(estimate.getEstName())
                .method(estimate.getMethod())
                .type(estimate.getType())
                .material(estimate.getMaterial())
                .thickness(estimate.getThickness())
                .surface(estimate.getSurface())
                .coatingColor(estimate.getCoatingColor())
                .isChamfer(estimate.isChamfer())
                .isFastShipment(estimate.isFastShipment())
                .unitPrice(estimate.getPrice())
                .cost(estimate.getCost())
                .kg(estimate.getKg())
                .commonDiff(estimate.getCommonDiff())
                .roughness(estimate.getRoughness())
                .holeJson(estimate.getHoleJson())
                .bbox(estimate.getBbox())
                .policyVersion(estimate.getPolicyVersion())
                .standardShipmentDay(estimate.getStandardShipmentDay())
                .expressShipmentDay(estimate.getExpressShipmentDay())
                /* ── 파일 관련 정보 ─────────────────────────────── */
                .fileKey(files.getFileKey())
                .fileName(estimate.getFileName())
                .s3StepAddress(files.getS3StepAddress())
                .s3DxfAddress(files.getS3DxfAddress())
                .factoryDxfAddress(files.getFactoryDxfAddress())
                .imageAddress(files.getImageAddress())
                .metaJson(files.getMetaJson())
                /* ── 기타 ──────────────────────────────────────── */
                .amount(amount)
                .build();
    }

    public CartItemDTO.GetCartResponse cartItemToCartItemResponse(CartItem cartItem) {
        return CartItemDTO.GetCartResponse.builder()
                .cartItemKey(cartItem.getCartItemKey())
                .estKey(cartItem.getEstKey())
                .estKey(cartItem.getEstKey())
                .fileName(cartItem.getFileName())
                .amount(cartItem.getAmount())
                .isChamfer(cartItem.isChamfer())
                .shipmentDate(cartItem.getShipmentDate())
                .s3StepAddress(cartItem.getS3StepAddress())
                .surface(cartItem.getSurface())
                .totalPrice(cartItem.getTotalPrice())
                .method(cartItem.getMethod())
                .unitPrice(cartItem.getUnitPrice())
                .discount(cartItem.getDiscount())
                .paymentPrice(cartItem.getPaymentPrice())
                .isFastShipment(cartItem.isFastShipment())
                .holeJson(cartItem.getHoleJson())
                .imageAddress(cartItem.getImageAddress())
                .material(cartItem.getMaterial())
                .standardShipmentDay(cartItem.getStandardShipmentDay())
                .expressShipmentDay(cartItem.getExpressShipmentDay())
                .build();
    }
}
