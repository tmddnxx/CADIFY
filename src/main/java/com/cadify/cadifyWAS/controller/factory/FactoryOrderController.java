package com.cadify.cadifyWAS.controller.factory;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.dto.admin.order.CalendarOrderCountsResponse;
import com.cadify.cadifyWAS.model.dto.factory.order.FactoryOrderDTO;
import com.cadify.cadifyWAS.model.dto.factory.order.OrderItemResponse;
import com.cadify.cadifyWAS.model.dto.factory.order.OrderResponse;
import com.cadify.cadifyWAS.model.dto.factory.order.RejectedItemResponse;
import com.cadify.cadifyWAS.service.factory.FactoryOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/factory/order")
public class FactoryOrderController {
    private final FactoryOrderService factoryOrderService;

    // 공장용 주문 조회
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersWithStatus(@RequestParam(name = "status", required = false) List<String> status){
        if(status == null){
            status = List.of();
        }
        List<OrderResponse> response = factoryOrderService.getOrders(status);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 주문 제작불가 판정
    @PatchMapping("/item/reject")
    public ResponseEntity<RejectedItemResponse> rejectOrderItem(@RequestBody FactoryOrderDTO.RejectRequest request){
        // 거절사유 Not Null
        if(request.getRejectReasonDetail() == null || request.getRejectReasonDetail().isEmpty()){
            throw new CustomLogicException(ExceptionCode.REJECTED_REASON_REQUIRED);
        }
        RejectedItemResponse response = factoryOrderService.rejectOrderItem(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 주문 아이템 송장 등록
    @PatchMapping("/item/shipment")
    public ResponseEntity<List<OrderItemResponse>> shippingOrderItem(@RequestBody FactoryOrderDTO.StartShippingRequest request){
        // 송장번호 입력, 담당 택배사, 아이텀 선택 확인
        if(request.getTrackingNumber() == null || request.getTrackingNumber().isEmpty()){
            throw new CustomLogicException(ExceptionCode.REQUIRED_TRACKING_NUMBER);
        }else if(request.getCourier() == null || request.getCourier().isEmpty()){
            throw new CustomLogicException(ExceptionCode.REQUIRED_COURIER_NAME);
        }else if(request.getItemKeyList() == null || request.getItemKeyList().isEmpty()){
            throw new CustomLogicException(ExceptionCode.REQUIRED_ITEM_KEYS);
        }

        List<OrderItemResponse> response = factoryOrderService.startShipping(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 주문 아이템 제작 가능 판정
    @PatchMapping("/item/confirm")
    public ResponseEntity<List<OrderItemResponse>> confirmOrderItem(@RequestBody FactoryOrderDTO.ConfirmRequest request){
        List<OrderItemResponse> response = factoryOrderService.confirmOrderItem(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 공장용 특정 주문에 포함된 아이템 조회
    @PostMapping("/items")
    public ResponseEntity<List<OrderItemResponse>> getOrderItems(@RequestBody FactoryOrderDTO.OrderItemRequest request){
        List<OrderItemResponse> response = factoryOrderService.getOrderItems(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 납기일 별 주문 수 조회
    @GetMapping("/shipment/count")
    public ResponseEntity<List<CalendarOrderCountsResponse>> getShipmentDateOrderCount(
            @RequestParam(value = "yearMonth") List<String> yearMonth
    ){
        List<CalendarOrderCountsResponse> response = factoryOrderService.getShipmentDateOrderCount(yearMonth);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 최근 정산 내역 조회
    @GetMapping("/settlement/recent")
    public ResponseEntity<List<OrderResponse>> getRecentlySettlementOrders(){

        List<OrderResponse> response = factoryOrderService.getRecentlySettlementOrders();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
