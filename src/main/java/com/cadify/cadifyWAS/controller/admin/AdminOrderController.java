package com.cadify.cadifyWAS.controller.admin;

import com.cadify.cadifyWAS.model.dto.admin.order.*;
import com.cadify.cadifyWAS.repository.admin.order.OrderColumn;
import com.cadify.cadifyWAS.service.admin.AdminOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/order")
@Log4j2
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    // 총 주문 : 주문 조회 ( filter )
    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> getTotalOrders(@ModelAttribute AdminOrderDTO.OrderRequest request){

        OrderColumn column;

        if(request.getDateBy().equalsIgnoreCase(OrderColumn.SHIPMENT.toString())){
            column = OrderColumn.SHIPMENT;
        }else{
            column = OrderColumn.CREATED;
        }

        List<OrderResponse> response = adminOrderService.getOrders(request, column);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 총 주문, 날짜별 주문 :  주문 > 주문 아이템 상세 리스트 조회
    @PostMapping("/items")
    public ResponseEntity<OrderItemResponse> getOrderItemsByOrderKey(@RequestBody AdminOrderDTO.OrderItemRequest request){

        OrderItemResponse response = adminOrderService.findOrderItemsByOrderKey(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 날짜별 주문 : 주문 수 조회
    @GetMapping("/date/count")
    public ResponseEntity<List<CalendarOrderCountsResponse>> getDateOrderCount(
            @RequestParam(value = "yearMonth") List<String> yearMonth){

        List<CalendarOrderCountsResponse> response = adminOrderService.getDateOrderCounts(yearMonth);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 납기일 주문 : 주문 수 조회
    @GetMapping("/delivery/count")
    public ResponseEntity<List<CalendarOrderCountsResponse>> getDeliveryOrderCount(
            @RequestParam(value = "yearMonth") String[] yearMonth){
        log.info("used");
        List<String> requestValue = Arrays.asList(yearMonth);

        List<CalendarOrderCountsResponse> response = adminOrderService.getDeliveryOrderCounts(requestValue);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 정산 관리 : 상태 카드 데이터
    @GetMapping("/settlement/cards")
    public ResponseEntity<SettlementCardsResponse> getSettlementCardsData(){
        SettlementCardsResponse response = adminOrderService.getSettlementCardsResponse();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // order item status update
    @PatchMapping("/item/status")
    public ResponseEntity<Map<String, String>> updateOrderItemStatus(@RequestBody AdminOrderDTO.StatusUpdateRequests request){
        String response = adminOrderService.decideProcessable(request);

        return new ResponseEntity<>(Map.of("status", response), HttpStatus.OK);
    }

    // order status update
    @PatchMapping("/status")
    public ResponseEntity<Map<String, String>> updateOrderStatus(@RequestBody AdminOrderDTO.StatusUpdateRequests request){
        String response = adminOrderService.processDeliveryAndSettlement(request);

        return new ResponseEntity<>(Map.of("status", response), HttpStatus.OK);
    }
}
