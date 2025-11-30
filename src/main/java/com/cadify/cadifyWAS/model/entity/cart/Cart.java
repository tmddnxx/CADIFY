package com.cadify.cadifyWAS.model.entity.cart;

import com.cadify.cadifyWAS.util.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@NoArgsConstructor
public class Cart extends BaseTimeEntity {

    @Id
    @GeneratedValue
    private Long cartKey;

    private String memberKey;

    private int deliveryCharge;

    private int vat;

    private int cartTotalPrice = 0;

    private int cartDiscount = 0;

    private int cartTotalPaymentPrice = 0;

    @Builder
    public Cart(String memberKey) {
        this.memberKey = memberKey;
    }


    public void recalculate(List<CartItem> cartItems) {

        Set<LocalDate> cncShipmentDate = new HashSet<>();
        Set<LocalDate> metalShipmentDate = new HashSet<>();

        for (CartItem cartItem : cartItems) {
            if (cartItem.getMethod().equals("cnc")) {
                cncShipmentDate.add(cartItem.getShipmentDate());
            } else if (cartItem.getMethod().equals("sheet_metal")) {
                metalShipmentDate.add(cartItem.getShipmentDate());
            }
        }

        int deliveryCharge = (cncShipmentDate.size() + metalShipmentDate.size()) * 7000;

        this.cartTotalPrice = cartItems.stream()
                .mapToInt(CartItem::getPaymentPrice)
                .sum();

        this.cartDiscount =cartItems.stream()
                .mapToInt(CartItem::getDiscount)
                .sum();

        int vat = (cartTotalPrice + deliveryCharge) / 10;

        this.deliveryCharge = deliveryCharge;
        this.vat = vat;
        this.cartTotalPaymentPrice = cartTotalPrice + deliveryCharge + vat;
    }


}
