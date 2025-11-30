package com.cadify.cadifyWAS.mapper;

import com.cadify.cadifyWAS.model.dto.cart.CartDTO;
import com.cadify.cadifyWAS.model.dto.cart.CartItemDTO;
import com.cadify.cadifyWAS.model.entity.cart.Cart;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartMapper {

    public CartDTO.GetCartResponse CartToGetCartResponse(Cart cart, List<CartItemDTO.GetCartResponse> cartItemDTOS) {

        return CartDTO.GetCartResponse.builder()
                .cartItemList(cartItemDTOS)
                .vat(cart.getVat())
                .cartTotalPrice(cart.getCartTotalPrice())
                .cartTotalPaymentPrice(cart.getCartTotalPaymentPrice())
                .deliveryCharge(cart.getDeliveryCharge())
                .build();

    }
}
