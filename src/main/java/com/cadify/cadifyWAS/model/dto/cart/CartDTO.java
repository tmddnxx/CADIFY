package com.cadify.cadifyWAS.model.dto.cart;

import com.cadify.cadifyWAS.model.entity.cart.Cart;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CartDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request{

        private List<EstKeyAmount> estKeyList;
        private boolean overwrite = false;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class EstKeyAmount {
            private String estKey;
            private int amount;
        }
    }

    @Getter
    @Builder
    public static class GetCartResponse{
        private List<CartItemDTO.GetCartResponse> cartItemList;
        private int cartTotalPrice;
        private int cartTotalPaymentPrice;
        private int deliveryCharge;
        private int vat;

    }


}
