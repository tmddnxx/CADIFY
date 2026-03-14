package com.cadify.cadifyWAS.model.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetCartResponse{
        private List<CartItemDTO.GetCartResponse> cartItemList;
        private int cartTotalPrice;
        private int cartTotalPaymentPrice;
        private int deliveryCharge;
        private int vat;

    }


}
