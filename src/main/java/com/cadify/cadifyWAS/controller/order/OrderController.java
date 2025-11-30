package com.cadify.cadifyWAS.controller.order;

import com.cadify.cadifyWAS.model.dto.order.AddressDTO;
import com.cadify.cadifyWAS.model.dto.order.OrdersDTO;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    // 주문 생성 API
    @PostMapping("")
    public ResponseEntity<OrdersDTO.CreateResponse> createOrder(@Valid @RequestBody OrdersDTO.Request request) {
        return ResponseEntity.ok().body(orderService.createOrder(request));
    }

    // 모든 주문 조회 API
    @GetMapping("/all")
    public ResponseEntity<List<OrdersDTO.AllResponse>> getAllOrder() {
        return ResponseEntity.ok().body(orderService.getAllOrder());
    }

    // 단건 주문 조회
    @GetMapping("/{orderKey}")
    public ResponseEntity<OrdersDTO.Response> getOrder(@PathVariable("orderKey") String orderKey) {
        return ResponseEntity.ok().body(orderService.getOrder(orderKey));
    }

    // 주문 삭제
    @DeleteMapping("/{orderKey}")
    public ResponseEntity<ResultResponse> deleteOrder(@PathVariable("orderKey") String orderKey) {
        orderService.deleteOrder(orderKey);
        return ResponseEntity.ok().body(ResultResponse.of(ResultCode.DELETE_ORDER_SUCCESS));
    }

    // 주소 목록 조회 API
    @GetMapping("/address")
    public ResponseEntity<List<AddressDTO.Response>> getMyAddress() {
        return ResponseEntity.ok().body(orderService.getMyAddress());
    }

    // 주소 변경
    @PatchMapping("/address/{addressKey}")
    public ResponseEntity<ResultResponse> updateAddress(@PathVariable String addressKey, @RequestBody AddressDTO.Request request) {
        orderService.updateAddress(addressKey, request);
        return ResponseEntity.ok().body(ResultResponse.of(ResultCode.UPDATE_ORDER_ADDRESS_SUCCESS));
    }

    // 주소 등록
    @PostMapping("/address")
    public ResponseEntity<ResultResponse> createAddress(@RequestBody AddressDTO.CreateRequest request) {
        String addressKey = orderService.createAddress(request);
        return ResponseEntity.ok().body(ResultResponse.of(ResultCode.CREATE_ORDER_ADDRESS_SUCCESS,addressKey));
    }

    //주소 삭제
    @DeleteMapping("/address/{addressKey}")
    public ResponseEntity<ResultResponse> deleteAddress(@PathVariable String addressKey) {
        orderService.deleteAddress(addressKey);
        return ResponseEntity.ok().body(ResultResponse.of(ResultCode.DELETE_ADDRESS_SUCCESS));
    }

}
