//package com.cadify.cadifyWAS.controller.factory;
//
//import java.util.List;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PatchMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.cadify.cadifyWAS.model.dto.factory.order.FactoryOrderDTO;
//import com.cadify.cadifyWAS.model.entity.order.OrderItem;
//import com.cadify.cadifyWAS.service.factory.FactoryService;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/factory")
//public class FactoryController {
//
//    private final FactoryService factoryService;
//
////    // 주문 목록 조회
////    @GetMapping("/orders")
////    public ResponseEntity<List<FactoryOrderDTO.BaseResponse>> getAllOrders() {
////        return ResponseEntity.ok(factoryService.getAllOrders());
////    }
//
//    // 주문 상세 조회
//    @GetMapping("/orders/{orderKey}")
//    public ResponseEntity<FactoryOrderDTO.DetailResponse> getOrderDetail(@PathVariable String orderKey) {
//        return ResponseEntity.ok(factoryService.getOrderDetail(orderKey));
//    }
//
//    // 배송 정보 입력
//    @PatchMapping("/orders/{orderKey}/{orderItemKey}/tracking")
//    public ResponseEntity<FactoryOrderDTO.TrackingResponse> addTrackingInfo(
//        @PathVariable String orderKey,
//        @PathVariable String orderItemKey,
//        @RequestBody FactoryOrderDTO.TrackingRequest request) {
//
//        OrderItem orderItem = factoryService.addTrackingInfo(orderItemKey, request);
//
//        return ResponseEntity.ok(FactoryOrderDTO.TrackingResponse.builder()
//            .orderKey(orderKey)
//            .orderItemKey(orderItemKey)
//            .trackingNumber(orderItem.getTrackingNumber())
//            .courier(orderItem.getCourier())
//            .status(orderItem.getOrderReceivedStatus())
//            .build());
//    }
//
//    // TODO: 정산 목록 조회
//}