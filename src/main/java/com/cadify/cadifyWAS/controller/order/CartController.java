package com.cadify.cadifyWAS.controller.order;

import com.cadify.cadifyWAS.model.dto.cart.CartDTO;
import com.cadify.cadifyWAS.model.dto.cart.CartItemDTO;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.order.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cadify.cadifyWAS.result.ResultCode.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    // 장바구니에 견적 추가
    @PostMapping("")
    public ResponseEntity<ResultResponse> addCartIem(@RequestBody CartDTO.Request cartRequest) {
        List<Long> dupCartItem = cartService.addCartIem(cartRequest);
        if(dupCartItem == null){
            return ResponseEntity.ok().body(ResultResponse.of(ADD_CART_ITEM_SUCCESS));
        } else {
            return ResponseEntity.ok().body(ResultResponse.of(ADD_CART_ITEM_DUPLICATED, dupCartItem));
        }
    }

    // 장바구니에서 견적 삭제
    @DeleteMapping("/{cartItemKey}")
    public ResponseEntity<ResultResponse> deleteCartItem(@PathVariable Long cartItemKey) {
        cartService.deleteCartItem(cartItemKey);
        return ResponseEntity.ok().body(ResultResponse.of(DELETE_CART_ITEM_SUCCESS));
    }

    // 장바구니 조회
    @GetMapping("")
    public ResponseEntity<CartDTO.GetCartResponse> getCart() {
        return ResponseEntity.ok().body(cartService.getCart());
    }

    // 장바구니에서 견적 수량 변경
    @PostMapping("/amount")
    public ResponseEntity<ResultResponse> updateCartItemAmount(@RequestBody CartItemDTO.UpdateAmount updateRequest) {
        cartService.updateCartItemAmount(updateRequest);
        return ResponseEntity.ok().body(ResultResponse.of(UPDATE_CART_ITEM_AMOUNT_SUCCESS));
    }
}
