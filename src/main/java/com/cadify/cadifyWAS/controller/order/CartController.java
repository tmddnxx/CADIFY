package com.cadify.cadifyWAS.controller.order;

import com.cadify.cadifyWAS.model.dto.cart.CartDTO;
import com.cadify.cadifyWAS.model.dto.cart.CartItemDTO;
import com.cadify.cadifyWAS.result.ResultResponse;

import static com.cadify.cadifyWAS.result.ResultCode.DELETE_CART_ITEM_SUCCESS;
import static com.cadify.cadifyWAS.result.ResultCode.UPDATE_CART_ITEM_AMOUNT_SUCCESS;
import com.cadify.cadifyWAS.service.order.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    // 장바구니에 견적 추가
    @PostMapping("")
    public ResponseEntity<ResultResponse> addCartIem(@Valid @RequestBody CartDTO.Request cartRequest) {
        return ResponseEntity.ok().body(cartService.addCartIem(cartRequest));
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
